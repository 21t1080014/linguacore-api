package com.nomnom.linguacore.service;

import com.nomnom.linguacore.dto.request.CreateReviewRequest;
import com.nomnom.linguacore.dto.response.CardResponse;
import com.nomnom.linguacore.dto.response.ReviewResultResponse;
import com.nomnom.linguacore.entity.ReviewLog;
import com.nomnom.linguacore.entity.ReviewState;
import com.nomnom.linguacore.exception.ResourceNotFoundException;
import com.nomnom.linguacore.mapper.CardMapper;
import com.nomnom.linguacore.repository.ReviewLogRepository;
import com.nomnom.linguacore.repository.ReviewStateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReviewService {
    private final ReviewStateRepository reviewStateRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final CardMapper cardMapper;
    private final Sm2Scheduler sm2Scheduler;
    public ReviewService(ReviewStateRepository reviewStateRepository, ReviewLogRepository reviewLogRepository, CardMapper cardMapper, Sm2Scheduler sm2Scheduler){
        this.reviewStateRepository = reviewStateRepository;
        this.reviewLogRepository = reviewLogRepository;
        this.cardMapper = cardMapper;
        this.sm2Scheduler = sm2Scheduler;
    }
    public List<CardResponse> getDueCards() {
        return reviewStateRepository.findByDueDateLessThanEqual(LocalDate.now())
                .stream()
                .map(state -> cardMapper.toResponse(state.getCard()))
                .toList();
    }
    @Transactional
    public ReviewResultResponse recordReview(CreateReviewRequest request){
        // 1. Tìm review_state của card
        ReviewState state = reviewStateRepository.findByCardId(request.getCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Review state not found for card: " + request.getCardId()));

        // 2. Đọc interval CŨ trước khi ghi đè (quan trọng!)
        int before = state.getIntervalDays();

        // 3. Chạy SM-2
        var result = sm2Scheduler.schedule(
                state.getEasiness(),
                state.getRepetitions(),
                state.getIntervalDays(),
                request.getGrade());

        // 4. Cập nhật state từ kết quả
        state.setEasiness(result.easiness())
                .setRepetitions(result.repetitions())
                .setIntervalDays(result.intervalDays())
                .setDueDate(result.dueDate());
        reviewStateRepository.save(state);

        // 5. Ghi log
        ReviewLog log = new ReviewLog()
                .setGrade(request.getGrade().shortValue())   // Integer → Short
                .setMode(request.getMode())
                .setIntervalBefore(before)
                .setIntervalAfter(result.intervalDays())
                .setDurationMs(request.getDurationMs())
                .setCard(state.getCard());
        reviewLogRepository.save(log);
        return new ReviewResultResponse(
                result.dueDate(),
                result.intervalDays(),
                result.repetitions(),
                result.easiness()
        );
    }

}
