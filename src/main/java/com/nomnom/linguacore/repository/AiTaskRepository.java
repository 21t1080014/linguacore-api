package com.nomnom.linguacore.repository;

import com.nomnom.linguacore.entity.AiTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiTaskRepository extends JpaRepository<AiTask, Long> {
    @Modifying
    @Query("delete from AiTask t where t.card.id = :cardId")
    void bulkDeleteByCardId(@Param("cardId") Long cardId);
    @Modifying
    @Query("delete from AiTask t where t.card.id in (select c.id from Card c where c.deck.id = :deckId)")
    void bulkDeleteByDeckId(@Param("deckId") Long deckId);
}