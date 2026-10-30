package com.nomnom.linguacore.repository;

import com.nomnom.linguacore.entity.ReviewState;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReviewStateRepository extends JpaRepository<ReviewState,Long> {
    @EntityGraph(attributePaths = "card")
    List<ReviewState> findByDueDateLessThanEqualAndCardDeckUserId(LocalDate date, Long userId);

    @EntityGraph(attributePaths = "card")
    Optional<ReviewState> findByCardIdAndCardDeckUserId(Long cardId, Long userId);
}
