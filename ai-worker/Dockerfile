# Linux + Python 3.14, bản rút gọn (không dùng alpine: psycopg2-binary không có bản dựng sẵn cho alpine)
FROM python:3.14-slim
# Tắt bộ đệm output để print hiện ngay trong "docker compose logs"
ENV PYTHONUNBUFFERED=1
WORKDIR /app
# Chép danh sách thư viện TRƯỚC, cài xong rồi mới chép code:
# sửa worker.py không phải cài lại thư viện (cache lớp, như COPY pom.xml bên Java)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY worker.py .
# Chạy bằng user thường thay vì root
RUN useradd --create-home worker
USER worker
CMD ["python", "worker.py"]