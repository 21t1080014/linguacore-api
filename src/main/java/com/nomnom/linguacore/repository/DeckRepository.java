package com.nomnom.linguacore.repository;

import com.nomnom.linguacore.entity.Deck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeckRepository extends JpaRepository<Deck,Long> {

}
