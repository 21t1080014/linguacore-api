package com.nomnom.linguacore.service;

import com.nomnom.linguacore.dto.request.CreateDeckRequest;
import com.nomnom.linguacore.dto.response.DeckResponse;
import com.nomnom.linguacore.entity.Deck;
import com.nomnom.linguacore.entity.User;
import com.nomnom.linguacore.exception.ResourceNotFoundException;
import com.nomnom.linguacore.exception.UnauthorizedException;
import com.nomnom.linguacore.repository.AiTaskRepository;
import com.nomnom.linguacore.repository.CardRepository;
import com.nomnom.linguacore.repository.DeckRepository;
import com.nomnom.linguacore.repository.ReviewLogRepository;
import com.nomnom.linguacore.repository.ReviewStateRepository;
import com.nomnom.linguacore.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeckService {
    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final ReviewStateRepository reviewStateRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final AiTaskRepository aiTaskRepository;

    public DeckService(UserRepository userRepository, DeckRepository deckRepository, CardRepository cardRepository,
                       ReviewStateRepository reviewStateRepository, ReviewLogRepository reviewLogRepository,
                       AiTaskRepository aiTaskRepository) {
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
        this.cardRepository = cardRepository;
        this.reviewStateRepository = reviewStateRepository;
        this.reviewLogRepository = reviewLogRepository;
        this.aiTaskRepository = aiTaskRepository;
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

    private DeckResponse toResponse(Deck saved) {
        return new DeckResponse(
                saved.getId(),
                saved.getName(),
                saved.getTargetLang(),
                saved.getDescription(),
                saved.getCreatedAt()
        );
    }

    public DeckResponse createDeck(CreateDeckRequest request) {
        Deck deck = new Deck()
                .setName(request.getName())
                .setTargetLang(request.getTargetLang())
                .setDescription(request.getDescription())
                .setUser(getCurrentUser());
        Deck saved = deckRepository.save(deck);
        return toResponse(saved);
    }

    public DeckResponse updateDeck(Long id, CreateDeckRequest request) {
        Deck deckEntity = findOwnedDeck(id);
        deckEntity.setName(request.getName());
        deckEntity.setDescription(request.getDescription());
        deckEntity.setTargetLang(request.getTargetLang());
        Deck updated = deckRepository.save(deckEntity);
        return toResponse(updated);
    }

    // Khoá ngoại đều NO ACTION → xoá từ ngọn xuống gốc: dữ liệu của thẻ → thẻ → deck.
    // Mỗi bảng chỉ MỘT câu DELETE dù deck có bao nhiêu thẻ.
    // @Transactional: lỗi ở bất kỳ câu nào thì cả 5 câu cùng huỷ → không bao giờ còn deck xoá dở.
    @Transactional
    public void deleteDeck(Long id) {
        Deck deck = findOwnedDeck(id);   // kiểm tra quyền TRƯỚC khi xoá bất cứ thứ gì
        aiTaskRepository.bulkDeleteByDeckId(id);
        reviewLogRepository.bulkDeleteByDeckId(id);
        reviewStateRepository.bulkDeleteByDeckId(id);
        cardRepository.bulkDeleteByDeckId(id);
        deckRepository.delete(deck);
    }

    public List<DeckResponse> getAllDecks() {
        return deckRepository.findByUserId(getCurrentUser().getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public DeckResponse getDeckById(Long id) {
        return toResponse(findOwnedDeck(id));
    }
}