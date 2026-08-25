package com.nomnom.linguacore.service;

import com.nomnom.linguacore.dto.request.CreateCardRequest;
import com.nomnom.linguacore.dto.request.UpdateCardRequest;
import com.nomnom.linguacore.dto.response.CardResponse;
import com.nomnom.linguacore.entity.Card;
import com.nomnom.linguacore.entity.Deck;
import com.nomnom.linguacore.entity.ReviewState;
import com.nomnom.linguacore.entity.User;
import com.nomnom.linguacore.exception.ResourceNotFoundException;
import com.nomnom.linguacore.exception.UnauthorizedException;
import com.nomnom.linguacore.mapper.CardMapper;
import com.nomnom.linguacore.repository.CardRepository;
import com.nomnom.linguacore.repository.DeckRepository;
import com.nomnom.linguacore.repository.ReviewStateRepository;
import com.nomnom.linguacore.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
@Service
public class CardService {
    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final UserRepository  userRepository;
    private final ReviewStateRepository reviewStateRepository;
    private final CardMapper cardMapper;
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
    private Card findOwnedCard(Long id){
        Card card = cardRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Card not found: " +id));
        if(!card.getDeck().getUser().getId().equals(getCurrentUser().getId())){
            throw new ResourceNotFoundException("Card not found: " + id);
        }
        return card;
    }

    public CardService(CardRepository cardRepository, DeckRepository deckRepository, UserRepository userRepository, ReviewStateRepository reviewStateRepository, CardMapper cardMapper) {
        this.cardRepository = cardRepository;
        this.deckRepository = deckRepository;
        this.userRepository = userRepository;
        this.reviewStateRepository = reviewStateRepository;
        this.cardMapper = cardMapper;
    }

    public List<CardResponse> getAllCards(Long deckId) {
        findOwnedDeck(deckId);
        List<Card> card = cardRepository.findByDeckId(deckId);
        return card.stream().map(cardMapper::toResponse).toList();
    }

    public CardResponse getCardById(Long cardId){
        Card card = findOwnedCard(cardId);
        return cardMapper.toResponse(card);
    }
    @Transactional
    public CardResponse createCard(Long deckId, CreateCardRequest request){
        Deck deck = findOwnedDeck(deckId);
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
       Card card = findOwnedCard(cardId);
       card.setBack(request.getBack())
               .setFront(request.getFront())
               .setPos(request.getPos())
               .setNote(request.getNote());
       Card saved = cardRepository.save(card);
       return cardMapper.toResponse(saved);
    }

    public void deleteCard(Long cardId){
        Card card = findOwnedCard(cardId);
        cardRepository.delete(card);
    }

}
