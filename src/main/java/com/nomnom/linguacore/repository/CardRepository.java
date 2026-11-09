package com.nomnom.linguacore.repository;

import com.nomnom.linguacore.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByDeckId(Long deckId);
    // Lọc chủ sở hữu ngay trong SQL: thẻ không tồn tại hay của người khác đều ra rỗng → cùng một 404
    Optional<Card> findByIdAndDeckUserId(Long id, Long userId);                        // AiTaskController: URL không có deckId
    Optional<Card> findByIdAndDeckIdAndDeckUserId(Long id, Long deckId, Long userId);  // /api/decks/{deckId}/cards/{cardId}
}
