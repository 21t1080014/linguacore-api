package com.nomnom.linguacore.controller;

import com.nomnom.linguacore.dto.request.CreateDeckRequest;
import com.nomnom.linguacore.dto.response.DeckResponse;
import com.nomnom.linguacore.service.DeckService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
public class DeckController {
    private final DeckService service;

    public DeckController(DeckService service) {
        this.service = service;
    }
    @PostMapping
    public ResponseEntity<DeckResponse> createDeck(@Valid @RequestBody CreateDeckRequest request){
        DeckResponse response = service.createDeck(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PutMapping("/{id}")
    public ResponseEntity<DeckResponse> updateDeck(@PathVariable Long id,@Valid @RequestBody CreateDeckRequest request){
        DeckResponse response = service.updateDeck(id, request);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeck(@PathVariable Long id){
        service.deleteDeck(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping
    public ResponseEntity<List<DeckResponse>> getAllDecks(){
        return ResponseEntity.ok(service.getAllDecks());
    }
    @GetMapping("/{id}")
    public ResponseEntity<DeckResponse> getDecById(@PathVariable Long id){
        return ResponseEntity.ok(service.getDeckById(id));
    }
}
