package com.nomnom.linguacore.repository;

import com.nomnom.linguacore.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByDeckId(Long deckId);
}
