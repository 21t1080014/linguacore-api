package com.nomnom.linguacore.mapper;

import com.nomnom.linguacore.dto.response.CardResponse;
import com.nomnom.linguacore.entity.Card;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {
    public CardResponse toResponse(Card card) {
        return new CardResponse(
                card.getId(),
                card.getFront(),
                card.getBack(),
                card.getPos(),
                card.getNote(),
                card.getCreatedAt(),
                card.getDeck().getId()
        );
    }
}
