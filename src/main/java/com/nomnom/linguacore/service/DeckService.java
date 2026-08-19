package com.nomnom.linguacore.service;

import com.nomnom.linguacore.dto.request.CreateDeckRequest;
import com.nomnom.linguacore.dto.response.DeckResponse;
import com.nomnom.linguacore.entity.Deck;
import com.nomnom.linguacore.exception.ResourceNotFoundException;
import com.nomnom.linguacore.repository.DeckRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeckService {
    private final DeckRepository deckRepository;

    private DeckResponse toResponse(Deck saved){
        return new DeckResponse(
                saved.getId(),
                saved.getName(),
                saved.getTargetLang(),
                saved.getDescription(),
                saved.getCreatedAt()
        );
    }

    public DeckService(DeckRepository deckRepository) {
        this.deckRepository = deckRepository;
    }

    public DeckResponse createDeck(CreateDeckRequest request){
        Deck deck = new Deck()
                .setName(request.getName())
                .setTargetLang(request.getTargetLang())
                .setDescription(request.getDescription());
        Deck saved = deckRepository.save(deck);
        return toResponse(saved);
    }

    public DeckResponse updateDeck(Long id,CreateDeckRequest request){
        Deck deckEntity = deckRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Deck not found: " + id));
        deckEntity.setName(request.getName());
        deckEntity.setDescription(request.getDescription());
        deckEntity.setTargetLang(request.getTargetLang());
        Deck updated = deckRepository.save(deckEntity);
        return toResponse(updated);
    }

    public void deleteDeck(Long id){
        Deck deck = deckRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Deck not found: " + id));
        deckRepository.delete(deck);
    }

   public List<DeckResponse> getAllDecks(){
        List<Deck> decks = deckRepository.findAll();
       return decks.stream()
               .map(this::toResponse)
               .toList();
   }

   public DeckResponse getDeckById(Long id){
        Deck deck = deckRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Deck not found: " + id));
        return toResponse(deck);
   }
}
