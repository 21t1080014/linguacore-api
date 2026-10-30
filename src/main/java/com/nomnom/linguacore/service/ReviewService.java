package com.nomnom.linguacore.service;

import com.nomnom.linguacore.dto.request.CreateReviewRequest;
import com.nomnom.linguacore.dto.response.CardResponse;
import com.nomnom.linguacore.dto.response.ReviewResultResponse;
import com.nomnom.linguacore.entity.ReviewLog;
import com.nomnom.linguacore.entity.ReviewState;
import com.nomnom.linguacore.entity.User;
import com.nomnom.linguacore.enums.AnswerType;
import com.nomnom.linguacore.exception.ResourceNotFoundException;
import com.nomnom.linguacore.exception.UnauthorizedException;
import com.nomnom.linguacore.mapper.CardMapper;
import com.nomnom.linguacore.repository.ReviewLogRepository;
import com.nomnom.linguacore.repository.ReviewStateRepository;
import com.nomnom.linguacore.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReviewService {
    private static final int GRADE_CORRECT = 4;  // SM-2: nhớ sau một lúc ngập ngừng → EF giữ nguyên
    private static final int GRADE_WRONG = 1;    // dưới 3 = quên → về đầu chu trình

    private final ReviewStateRepository reviewStateRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final Sm2Scheduler sm2Scheduler;
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Không xác định được người dùng"));
    }
    public ReviewService(ReviewStateRepository reviewStateRepository, ReviewLogRepository reviewLogRepository, UserRepository userRepository, CardMapper cardMapper, Sm2Scheduler sm2Scheduler){
        this.reviewStateRepository = reviewStateRepository;
        this.reviewLogRepository = reviewLogRepository;
        this.userRepository = userRepository;
        this.cardMapper = cardMapper;
        this.sm2Scheduler = sm2Scheduler;
    }
    public List<CardResponse> getDueCards() {
        return reviewStateRepository.findByDueDateLessThanEqualAndCardDeckUserId(LocalDate.now() , getCurrentUser().getId())
                .stream()
                .map(state -> cardMapper.toResponse(state.getCard()))
                .toList();
    }

    @Transactional
    public ReviewResultResponse recordReview(CreateReviewRequest request) {
        // 1 truy vấn: thẻ không tồn tại HOẶC của người khác đều ra rỗng → cùng một 404
        ReviewState state = reviewStateRepository
                .findByCardIdAndCardDeckUserId(request.getCardId(), getCurrentUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + request.getCardId()));

        boolean typed = request.getMode().getAnswerType() == AnswerType.TYPED;
        String expected = typed ? state.getCard().getFront() : null;
        Boolean correct = typed ? AnswerChecker.isCorrect(request.getTypedAnswer(), expected) : null;

        // grade client gửi (ghi đè) được ưu tiên; không có thì server suy ra từ đúng/sai.
        // Validation đã bảo đảm: grade == null thì chắc chắn là chế độ gõ, nên `correct` khác null.
        int grade = request.getGrade() != null ? request.getGrade() : (correct ? GRADE_CORRECT : GRADE_WRONG);

        int before = state.getIntervalDays();   // đọc interval CŨ trước khi bị ghi đè
        var result = sm2Scheduler.schedule(state.getEasiness(), state.getRepetitions(), before, grade);
        state.setEasiness(result.easiness())
                .setRepetitions(result.repetitions())
                .setIntervalDays(result.intervalDays())
                .setDueDate(result.dueDate());
        // Không cần reviewStateRepository.save(state): entity đang được Hibernate theo dõi
        // trong @Transactional, lúc commit nó tự UPDATE phần đã đổi (dirty checking).

        reviewLogRepository.save(new ReviewLog()
                .setGrade((short) grade)
                .setMode(request.getMode().name())
                .setIntervalBefore(before)
                .setIntervalAfter(result.intervalDays())
                .setDurationMs(request.getDurationMs())
                .setCard(state.getCard())
                .setCorrect(correct));

        return new ReviewResultResponse(
                result.dueDate(),
                result.intervalDays(),
                result.repetitions(),
                result.easiness(), correct, expected);
    }

}
