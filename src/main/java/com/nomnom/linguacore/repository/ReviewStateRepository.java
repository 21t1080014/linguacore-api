package com.nomnom.linguacore.repository;

import com.nomnom.linguacore.entity.ReviewState;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReviewStateRepository extends JpaRepository<ReviewState,Long> {
    @EntityGraph(attributePaths = "card")
    List<ReviewState> findByDueDateLessThanEqualAndCardDeckUserId(LocalDate date, Long userId);

    @EntityGraph(attributePaths = "card")
    Optional<ReviewState> findByCardIdAndCardDeckUserId(Long cardId, Long userId);
    @Modifying
    @Query("delete from ReviewState s where s.card.id = :cardId")
    void bulkDeleteByCardId(@Param("cardId") Long cardId);
}
