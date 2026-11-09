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
import com.nomnom.linguacore.repository.AiTaskRepository;
import com.nomnom.linguacore.repository.CardRepository;
import com.nomnom.linguacore.repository.DeckRepository;
import com.nomnom.linguacore.repository.ReviewLogRepository;
import com.nomnom.linguacore.repository.ReviewStateRepository;
import com.nomnom.linguacore.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class CardService {
    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final UserRepository userRepository;
    private final ReviewStateRepository reviewStateRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final AiTaskRepository aiTaskRepository;
    private final CardMapper cardMapper;

    public CardService(CardRepository cardRepository, DeckRepository deckRepository, UserRepository userRepository,
                       ReviewStateRepository reviewStateRepository, ReviewLogRepository reviewLogRepository,
                       AiTaskRepository aiTaskRepository, CardMapper cardMapper) {
        this.cardRepository = cardRepository;
        this.deckRepository = deckRepository;
        this.userRepository = userRepository;
        this.reviewStateRepository = reviewStateRepository;
        this.reviewLogRepository = reviewLogRepository;
        this.aiTaskRepository = aiTaskRepository;
        this.cardMapper = cardMapper;
    }

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

    // AiTaskController gọi hàm này (bean khác) → giữ public
    public Card findOwnedCard(Long cardId) {
        return cardRepository.findByIdAndDeckUserId(cardId, getCurrentUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardId));
    }

    // Dùng nội bộ cho các endpoint nằm dưới /api/decks/{deckId}/cards
    private Card findOwnedCardInDeck(Long deckId, Long cardId) {
        return cardRepository.findByIdAndDeckIdAndDeckUserId(cardId, deckId, getCurrentUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardId));
    }

    public List<CardResponse> getAllCards(Long deckId) {
        findOwnedDeck(deckId);
        return cardRepository.findByDeckId(deckId).stream().map(cardMapper::toResponse).toList();
    }

    public CardResponse getCardById(Long deckId, Long cardId) {
        return cardMapper.toResponse(findOwnedCardInDeck(deckId, cardId));
    }

    @Transactional
    public CardResponse createCard(Long deckId, CreateCardRequest request) {
        Deck deck = findOwnedDeck(deckId);
        Card saved = cardRepository.save(new Card()
                .setBack(request.getBack())
                .setFront(request.getFront())
                .setPos(request.getPos())
                .setNote(request.getNote())
                .setDeck(deck));
        reviewStateRepository.save(new ReviewState()
                .setCard(saved)
                .setEasiness(new BigDecimal("2.5"))
                .setRepetitions(0)
                .setIntervalDays(0)
                .setDueDate(LocalDate.now()));
        return cardMapper.toResponse(saved);
    }

    @Transactional   // dirty checking: card đang được theo dõi → lúc commit tự UPDATE, không cần save()
    public CardResponse updateCard(Long deckId, Long cardId, UpdateCardRequest request) {
        Card card = findOwnedCardInDeck(deckId, cardId);
        card.setBack(request.getBack())
                .setFront(request.getFront())
                .setPos(request.getPos())
                .setNote(request.getNote());
        return cardMapper.toResponse(card);
    }

    // Khoá ngoại đều NO ACTION → phải xoá bảng con trước, bảng cha (cards) sau cùng.
    // @Transactional: 4 câu DELETE cùng thành công, hoặc lỗi giữa chừng thì cùng huỷ → không còn dữ liệu xoá dở.
    @Transactional
    public void deleteCard(Long deckId, Long cardId) {
        Card card = findOwnedCardInDeck(deckId, cardId);   // kiểm tra quyền TRƯỚC khi xoá bất cứ thứ gì
        aiTaskRepository.bulkDeleteByCardId(cardId);
        reviewLogRepository.bulkDeleteByCardId(cardId);
        reviewStateRepository.bulkDeleteByCardId(cardId);
        cardRepository.delete(card);
    }
}