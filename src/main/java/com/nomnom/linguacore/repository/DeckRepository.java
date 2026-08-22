package com.nomnom.linguacore.repository;

import com.nomnom.linguacore.entity.Deck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeckRepository extends JpaRepository<Deck,Long> {
    List<Deck> findByUserId(Long userId);
}
