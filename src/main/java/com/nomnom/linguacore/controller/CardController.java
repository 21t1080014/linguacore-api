package com.nomnom.linguacore.controller;

import com.nomnom.linguacore.dto.request.CreateCardRequest;
import com.nomnom.linguacore.dto.request.UpdateCardRequest;
import com.nomnom.linguacore.dto.response.CardResponse;
import com.nomnom.linguacore.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks/{deckId}/cards")
public class CardController {
    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public ResponseEntity<CardResponse> createCard(@PathVariable Long deckId, @Valid @RequestBody CreateCardRequest request){
        CardResponse cardResponse = cardService.createCard(deckId,request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cardResponse);
    }
    @PutMapping("/{cardId}")
    public ResponseEntity<CardResponse> updateCard(@PathVariable Long cardId, @Valid @RequestBody UpdateCardRequest request){
        CardResponse cardResponse = cardService.updateCard(cardId,request);
        return ResponseEntity.ok(cardResponse);
    }

    @GetMapping
    public ResponseEntity<List<CardResponse>> getAllCards(@PathVariable Long deckId){
        return ResponseEntity.ok(cardService.getAllCards(deckId));
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<CardResponse> getCardById(@PathVariable  Long cardId ){
        return ResponseEntity.ok(cardService.getCardById(cardId));
    }
    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId){
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }
}
