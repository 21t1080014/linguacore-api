package com.nomnom.linguacore.service;

import com.nomnom.linguacore.dto.request.CreateDeckRequest;
import com.nomnom.linguacore.dto.response.DeckResponse;
import com.nomnom.linguacore.entity.Deck;
import com.nomnom.linguacore.entity.User;
import com.nomnom.linguacore.exception.ResourceNotFoundException;
import com.nomnom.linguacore.exception.UnauthorizedException;
import com.nomnom.linguacore.repository.DeckRepository;
import com.nomnom.linguacore.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeckService {
    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Không xác định được người dùng"));
    }
    private Deck findOwnedDeck(Long id) {
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found: " + id));
        if (!deck.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ResourceNotFoundException("Deck not found: " + id);
        }
        return deck;
    }

    private DeckResponse toResponse(Deck saved){
        return new DeckResponse(
                saved.getId(),
                saved.getName(),
                saved.getTargetLang(),
                saved.getDescription(),
                saved.getCreatedAt()
        );
    }

    public DeckService(UserRepository userRepository, DeckRepository deckRepository) {
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
    }

    public DeckResponse createDeck(CreateDeckRequest request){
        Deck deck = new Deck()
                .setName(request.getName())
                .setTargetLang(request.getTargetLang())
                .setDescription(request.getDescription())
                .setUser(getCurrentUser());
        Deck saved = deckRepository.save(deck);
        return toResponse(saved);
    }

    public DeckResponse updateDeck(Long id,CreateDeckRequest request){
        Deck deckEntity = findOwnedDeck(id);
        deckEntity.setName(request.getName());
        deckEntity.setDescription(request.getDescription());
        deckEntity.setTargetLang(request.getTargetLang());
        Deck updated = deckRepository.save(deckEntity);
        return toResponse(updated);
    }

    public void deleteDeck(Long id){
        Deck deck = findOwnedDeck(id);
        deckRepository.delete(deck);
    }

   public List<DeckResponse> getAllDecks(){
       return deckRepository.findByUserId(getCurrentUser().getId())
               .stream()
               .map(this::toResponse)
               .toList();
   }

   public DeckResponse getDeckById(Long id){
       Deck deck = findOwnedDeck(id);
        return toResponse(deck);
   }
}
