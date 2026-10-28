"""
worker.py — Worker xử lý "AI task" cho ứng dụng học từ vựng (LinguaCore).

VAI TRÒ TRONG HỆ THỐNG
    Backend chính (vd Spring) khi cần AI sinh câu ví dụ cho một thẻ từ vựng sẽ:
        1. Tạo 1 dòng trong bảng ai_task (status chưa DONE) gắn với thẻ (card_id).
        2. Gửi ID của task đó vào hàng đợi RabbitMQ "ai.task.queue".
    Worker này (chạy riêng, liên tục):
        3. Nhận task_id từ hàng đợi.
        4. Đọc từ tiếng Anh (cards.front) trong PostgreSQL.
        5. Gọi Gemini để sinh 1 câu ví dụ dùng từ đó.
        6. Ghi kết quả vào ai_task.result, đặt status = 'DONE'.

    Sơ đồ:
        Backend --task_id--> [ai.task.queue] --> worker.py --> Gemini
                                                    |
                                                    +--> PostgreSQL (đọc cards, ghi ai_task)

XỬ LÝ LỖI (retry + DLQ)
    Nếu xử lý lỗi (Gemini lỗi, mất mạng...):
        - Tăng retry_count, chờ 2^n giây (2s, 4s, 8s) rồi gửi lại task vào hàng đợi chính.
        - Sau khi thất bại quá MAX_RETRIES lần thử lại -> đặt status = 'FAILED' và chuyển
          task sang "ai.task.dlq" (Dead Letter Queue = nơi chứa task hỏng để người kiểm tra sau).

BẢNG DB ĐƯỢC DÙNG (chỉ liệt kê cột mà file này đụng tới)
    cards   : id, front (từ tiếng Anh)
    ai_task : id, card_id, status, result, retry_count, updated_at

YÊU CẦU ĐỂ CHẠY
    - RabbitMQ ở localhost:5672, PostgreSQL ở localhost:5433 (database "linguadb").
    - File .env có: RABBITMQ_USER, RABBITMQ_PASSWORD, DB_USERNAME, DB_PASSWORD, GEMINI_API_KEY.
    - python worker.py   (chạy mãi, Ctrl+C để dừng)
"""

from dotenv import load_dotenv   # đọc file .env
import os                        # đọc biến môi trường
import time                      # time.sleep() để chờ trước khi thử lại
import pika                      # client RabbitMQ
import psycopg2                  # driver kết nối PostgreSQL
import redis
from google import genai         # SDK gọi Gemini

# ===========================================================================
# 1. CẤU HÌNH
# ===========================================================================
load_dotenv()

# Thông tin bí mật lấy từ .env (không ghi cứng trong code để tránh lộ khi đẩy lên Git)
RABBITMQ_USER = os.getenv("RABBITMQ_USER")
RABBITMQ_PASSWORD = os.getenv("RABBITMQ_PASSWORD")
DB_USERNAME = os.getenv("DB_USERNAME")
DB_PASSWORD = os.getenv("DB_PASSWORD")
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
DB_HOST = os.getenv("DB_HOST", "localhost")
DB_PORT = os.getenv("DB_PORT", "5433")        # chuỗi; psycopg2 nhận cổng dạng chuỗi
RABBITMQ_HOST = os.getenv("RABBITMQ_HOST", "localhost")

QUEUE = "ai.task.queue"   # hàng đợi chính: các task đang chờ xử lý
DLQ = "ai.task.dlq"       # Dead Letter Queue: task đã thất bại hẳn
MAX_RETRIES = 3           # số lần thử lại tối đa trước khi bỏ cuộc

REDIS_HOST = os.getenv("REDIS_HOST", "localhost")
REDIS_PASSWORD = os.getenv("REDIS_PASSWORD")
CACHE_TTL = 30 * 24 * 3600   # 30 ngày, tính bằng giây
CACHE_COOLDOWN = 60      # giây: sau khi Redis lỗi, bỏ qua cache trong khoảng này
cache_down_until = 0     # mốc thời gian: trước mốc này thì không dùng cache

# decode_responses=True: Redis trả bytes, bật cờ này để get() trả str (giống .decode() với body RabbitMQ).
# timeout 2s: nếu Redis chết thì Worker không bị treo chờ.
# Client chưa kết nối ngay, chỉ kết nối khi gọi lệnh đầu tiên, nên Worker vẫn khởi động được khi Redis hỏng.
cache = redis.Redis(host=REDIS_HOST, password=REDIS_PASSWORD, decode_responses=True,
                    socket_connect_timeout=2, socket_timeout=2)

# ===========================================================================
# 2. KẾT NỐI DÙNG CHUNG (tạo 1 lần, dùng cho mọi task)
# ===========================================================================
# MỚI: mở kết nối DB một lần duy nhất lúc Worker khởi động, dùng lại cho MỌI
# task xử lý sau này — không mở/đóng connection mỗi lần nhận message (tốn kém,
# và Worker chạy vòng lặp vô hạn nên không có "lúc thích hợp" để đóng giữa chừng).
db_conn = psycopg2.connect(
    host=DB_HOST, port=DB_PORT, dbname="linguadb",
    user=DB_USERNAME, password=DB_PASSWORD
)
# Client Gemini cũng tạo 1 lần rồi dùng lại.
gemini_client = genai.Client(api_key=GEMINI_API_KEY)

# ===========================================================================
# 3. CÁC HÀM XỬ LÝ
# ===========================================================================
def publish_task(ch, queue, task_id):
    """
    Gửi task_id vào hàng đợi `queue` (dùng để gửi lại vào QUEUE khi retry,
    hoặc chuyển sang DLQ khi thất bại hẳn).
    Nội dung tin nhắn chỉ là ID dạng chuỗi bytes, vd b"42" — dữ liệu thật nằm trong DB.
    delivery_mode=2: tin nhắn bền, không mất khi RabbitMQ khởi động lại.
    """
    ch.basic_publish(
        exchange="",
        routing_key=queue,
        body=str(task_id).encode(),
        properties=pika.BasicProperties(delivery_mode=2)
    )
def cache_call(func, *args, **kwargs):
    # func là hàm Redis được truyền vào (như method reference bên Java): cache.get hoặc cache.set.
    # `global`: bắt buộc khi GÁN lại biến ở ngoài hàm. Thiếu nó, Python tạo ra một biến cục bộ cùng tên
    # và biến ở ngoài không đổi (bẫy kinh điển của Python).
    global cache_down_until
    # monotonic: đồng hồ chỉ chạy tiến, không bị nhảy khi hệ thống chỉnh giờ.
    if time.monotonic() < cache_down_until:
        return None                              # đang tạm ngừng: bỏ qua cache
    try:
        return func(*args, **kwargs)             # chạy lệnh Redis thật
    except redis.RedisError as e:
        cache_down_until = time.monotonic() + CACHE_COOLDOWN
        print(f"Redis lỗi, tạm bỏ qua cache {CACHE_COOLDOWN}s: {e}")
        return None

def process_task(task_id):
    """
    Xử lý 1 task: đọc từ vựng -> gọi Gemini -> lưu kết quả.
    Nếu có lỗi ở bất kỳ bước nào, hàm sẽ ném Exception ra ngoài để callback() bắt
    và chuyển sang handle_failure().
    """
    with db_conn.cursor() as cur:  # đã học: with tự đóng cursor kể cả khi lỗi
        # Bước 1: lấy từ tiếng Anh (front) của thẻ gắn với task, kèm trạng thái task.
        # %s là chỗ giữ chỗ — psycopg2 tự chèn giá trị an toàn (chống SQL injection),
        # KHÔNG được tự ghép chuỗi bằng f-string vào câu SQL.
        # (task_id,) có dấu phẩy để Python hiểu đây là tuple 1 phần tử.
        cur.execute(
            """
            SELECT cards.front, ai_task.status
            FROM ai_task
            JOIN cards ON ai_task.card_id = cards.id
            WHERE ai_task.id = %s
            """,
            (task_id,)
        )
        row = cur.fetchone()   # lấy 1 dòng kết quả, trả None nếu không có
        if row is None:
            print(f"Không tìm thấy task {task_id}")
            return
        front,status=row       # tách tuple (front, status) thành 2 biến

        # Bước 2: kiểm tra trùng lặp. RabbitMQ có thể giao 1 tin nhắn nhiều lần
        # (vd worker chết trước khi ack) -> task đã DONE thì bỏ qua, không gọi Gemini lại.
        if status == "DONE":
            print(f"Task {task_id} đã xong từ trước, bỏ qua")
            return

        # Bước 3: lấy câu ví dụ — ưu tiên cache, không có mới gọi Gemini.
        # Khoá chuẩn hoá: "Hello" và " hello " dùng chung; đổi v1 khi sửa câu lệnh gửi Gemini.
        key = f"ai:example:v1:{front.strip().lower()}"
        result_text = cache_call(cache.get, key)
        if not result_text:   # không có (hoặc rỗng): gọi Gemini rồi lưu
            interaction = gemini_client.interactions.create(
                model="gemini-3.5-flash-lite",
                # model="gemini-sai-ten",   # bật dòng này để giả lập lỗi, test cơ chế retry/DLQ
                input=f"Cho một câu ví dụ tiếng Anh dùng từ '{front}', chỉ trả đúng 1 câu, không giải thích thêm."
            )
            result_text = interaction.output_text
            cache_call(cache.set, key, result_text, ex=CACHE_TTL)
        else:
            print(f"Cache hit: {key}")

        # Bước 4: lưu kết quả và đánh dấu task hoàn thành.
        cur.execute(
            """
            UPDATE ai_task
            SET result = %s, status = 'DONE', updated_at = now()
            WHERE id = %s
            """,
            (result_text, task_id)
        )
    # MỚI: psycopg2 không tự lưu thay đổi vào DB ngay sau execute — phải tự
    # gọi commit() thì UPDATE mới thật sự ghi xuống. Giống vai trò dấu chấm dứt
    # một giao dịch mà @Transactional bên Spring tự làm giúp bạn; ở đây làm tay.
    db_conn.commit()
    print(f"Task {task_id} xong: {result_text}")

def handle_failure(ch, task_id):
    """
    Gọi khi process_task() bị lỗi. Quyết định: thử lại sau, hay bỏ hẳn sang DLQ.
        - retry_count <= MAX_RETRIES : chờ 2^retry_count giây rồi gửi lại vào QUEUE.
        - retry_count >  MAX_RETRIES : đặt status = 'FAILED' và gửi sang DLQ.
    """
    with db_conn.cursor() as cur:
        # RETURNING: cập nhật xong trả luôn retry_count mới, khỏi SELECT lại
        cur.execute(
            """
            UPDATE ai_task
            SET retry_count = retry_count + 1, updated_at = now()
            WHERE id = %s
            RETURNING retry_count
            """,
            (task_id,)
        )
        retry_count = cur.fetchone()[0]   # [0] = cột đầu tiên (retry_count) của dòng trả về
        exhausted = retry_count > MAX_RETRIES   # đã thất bại quá 3 lần thử lại?
        if exhausted:
            cur.execute(
                "UPDATE ai_task SET status = 'FAILED', updated_at = now() WHERE id = %s",
                (task_id,)
            )
    db_conn.commit()   # lưu retry_count (và status FAILED nếu có) xuống DB

    if exhausted:
        print(f"Task {task_id} thất bại hẳn (lỗi lần {retry_count}) -> chuyển DLQ")
        publish_task(ch, DLQ, task_id)
    else:
        # Exponential backoff: mỗi lần lỗi chờ lâu gấp đôi, cho dịch vụ bên ngoài
        # (Gemini) thời gian hồi phục thay vì gọi dồn dập.
        # Lưu ý: time.sleep() chặn cả worker trong lúc chờ (không xử lý task khác).
        wait = 2 ** retry_count    # luỹ thừa: 2s, 4s, 8s
        print(f"Task {task_id} lỗi lần {retry_count}, thử lại sau {wait}s")
        time.sleep(wait)
        publish_task(ch, QUEUE, task_id)   # gửi lại vào cuối hàng đợi chính

def callback(ch, method, properties, body):
    """
    pika tự gọi hàm này mỗi khi có 1 tin nhắn từ QUEUE.
    body là task_id dạng bytes (vd b"42") -> decode thành chuỗi -> int.
    """
    task_id = int(body.decode())
    print(f"Nhận task: {task_id}")
    try:
        process_task(task_id)
    except Exception as e:
        print(f"Lỗi xử lý task {task_id}: {e}")
        # rollback: huỷ các thay đổi DB chưa commit của task lỗi. Bắt buộc với psycopg2 —
        # sau lỗi, transaction ở trạng thái "hỏng" và mọi lệnh SQL tiếp theo sẽ bị từ chối
        # cho tới khi rollback.
        db_conn.rollback()
        handle_failure(ch, task_id)
    # Luôn ack (kể cả khi lỗi): tin nhắn GỐC được xoá khỏi hàng đợi, vì nếu cần thử lại
    # thì handle_failure() đã gửi một tin nhắn MỚI rồi — tránh bị xử lý trùng.
    ch.basic_ack(delivery_tag=method.delivery_tag)


# ===========================================================================
# 4. KHỞI ĐỘNG WORKER: kết nối RabbitMQ và bắt đầu nghe hàng đợi
# ===========================================================================
credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASSWORD)
connection = pika.BlockingConnection(
    pika.ConnectionParameters(host=RABBITMQ_HOST, port=5672, credentials=credentials, heartbeat=600)
)
channel = connection.channel()
# Khai báo cả 2 hàng đợi (tạo nếu chưa có). durable=True: hàng đợi tồn tại sau restart.
channel.queue_declare(queue=QUEUE, durable=True)
channel.queue_declare(queue=DLQ, durable=True)
# MỚI: basic_qos(prefetch_count=1) — chỉ nhận 1 message chưa-ack tại một thời
# điểm. Không có dòng này, RabbitMQ có thể dồn nhiều message tới Worker cùng lúc
# trước khi Worker kịp xử lý xong cái đầu tiên (mỗi lần gọi Gemini mất vài giây).
channel.basic_qos(prefetch_count=1)
# auto_ack=False: tự ack thủ công trong callback sau khi xử lý xong.
channel.basic_consume(queue=QUEUE, on_message_callback=callback, auto_ack=False)

print("Worker đang chạy, chờ task...")
# Vòng lặp vô hạn: chờ tin nhắn và gọi callback() cho từng cái. Ctrl+C để dừng.
channel.start_consuming()
