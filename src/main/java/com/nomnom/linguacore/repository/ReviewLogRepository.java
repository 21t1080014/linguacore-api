package com.nomnom.linguacore.repository;

import com.nomnom.linguacore.entity.ReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {
    @Modifying   // câu thay đổi dữ liệu, không phải SELECT
    @Query("delete from ReviewLog l where l.card.id = :cardId")   // 1 câu SQL xoá mọi dòng, không nạp từng dòng lên
    void bulkDeleteByCardId(@Param("cardId") Long cardId);
    @Modifying
    @Query("delete from ReviewLog l where l.card.id in (select c.id from Card c where c.deck.id = :deckId)")
    void bulkDeleteByDeckId(@Param("deckId") Long deckId);
}