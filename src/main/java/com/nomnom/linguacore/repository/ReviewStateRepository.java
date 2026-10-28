package com.nomnom.linguacore.repository;

import com.nomnom.linguacore.entity.ReviewState;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReviewStateRepository extends JpaRepository<ReviewState,Long> {
    List<ReviewState> findByDueDateLessThanEqual(LocalDate date);
    Optional<ReviewState> findByCardId(Long cardId);
//    List<ReviewState> findByDueDateLessThanEqualAndCardDeckUserId(LocalDate date, Long userId);
    // Đã có: chỉ thêm dòng @EntityGraph phía trên
    @EntityGraph(attributePaths = "card")
    List<ReviewState> findByDueDateLessThanEqualAndCardDeckUserId(LocalDate date, Long userId);

    // MỚI: thay cho findByCardId + đoạn kiểm tra chủ sở hữu trong service
    @EntityGraph(attributePaths = "card")
    Optional<ReviewState> findByCardIdAndCardDeckUserId(Long cardId, Long userId);
}
