package com.nomnom.linguacore.repository;

import com.nomnom.linguacore.entity.Deck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeckRepository extends JpaRepository<Deck,Long> {
    List<Deck> findByUserId(Long userId);
    Optional<Deck> findByIdAndUserId(Long id, Long userId);   // không tồn tại hay của người khác đều ra rỗng → cùng một 404
}
