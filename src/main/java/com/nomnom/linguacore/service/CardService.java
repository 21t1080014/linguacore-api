package com.nomnom.linguacore.service;

import com.nomnom.linguacore.dto.request.CreateCardRequest;
import com.nomnom.linguacore.dto.request.UpdateCardRequest;
import com.nomnom.linguacore.dto.response.CardResponse;
import com.nomnom.linguacore.entity.Card;
import com.nomnom.linguacore.entity.Deck;
import com.nomnom.linguacore.entity.ReviewState;
import com.nomnom.linguacore.exception.ResourceNotFoundException;
import com.nomnom.linguacore.mapper.CardMapper;
import com.nomnom.linguacore.repository.CardRepository;
import com.nomnom.linguacore.repository.DeckRepository;
import com.nomnom.linguacore.repository.ReviewStateRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
@Service
public class CardService {
    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final ReviewStateRepository reviewStateRepository;
    private final CardMapper cardMapper;

    public CardService(CardRepository cardRepository, DeckRepository deckRepository, ReviewStateRepository reviewStateRepository, CardMapper cardMapper) {
        this.cardRepository = cardRepository;
        this.deckRepository = deckRepository;
        this.reviewStateRepository = reviewStateRepository;
        this.cardMapper = cardMapper;
    }

    public List<CardResponse> getAllCards(Long deckId) {
        List<Card> card = cardRepository.findByDeckId(deckId);
        return card.stream().map(cardMapper::toResponse).toList();
    }

    public List<CardResponse> getDueCards() {
        return reviewStateRepository.findByDueDateLessThanEqual(LocalDate.now())
                .stream()
                .map(state -> cardMapper.toResponse(state.getCard()))
                .toList();
    }

    public CardResponse getCardById(Long cardId){
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardId));
        return cardMapper.toResponse(card);
    }
    @Transactional
    public CardResponse createCard(Long deckId, CreateCardRequest request){
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found: " + deckId));
        Card card = new Card()
                .setBack(request.getBack())
                .setFront(request.getFront())
                .setPos(request.getPos())
                .setNote(request.getNote())
                .setDeck(deck);
        Card saved = cardRepository.save(card);
        ReviewState state = new ReviewState()
                .setCard(saved)
                .setEasiness(new BigDecimal("2.5"))
                .setRepetitions(0)
                .setIntervalDays(0)
                .setDueDate(LocalDate.now());
        reviewStateRepository.save(state);
        return cardMapper.toResponse(saved);
    }

    public CardResponse updateCard(Long cardId, UpdateCardRequest request){
       Card card = cardRepository.findById(cardId).orElseThrow(()-> new ResourceNotFoundException("Card not found: " + cardId));
       card.setBack(request.getBack())
               .setFront(request.getFront())
               .setPos(request.getPos())
               .setNote(request.getNote());
       Card saved = cardRepository.save(card);
       return cardMapper.toResponse(saved);
    }

    public void deleteCard(Long cardId){
        Card card = cardRepository.findById(cardId).orElseThrow(()-> new ResourceNotFoundException("Card not found: " + cardId));
        cardRepository.delete(card);
    }

}
