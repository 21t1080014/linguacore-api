package com.nomnom.linguacore.repository;

import com.nomnom.linguacore.entity.ReviewState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReviewStateRepository extends JpaRepository<ReviewState,Long> {
    List<ReviewState> findByDueDateLessThanEqual(LocalDate date);
    Optional<ReviewState> findByCardId(Long cardId);
}
