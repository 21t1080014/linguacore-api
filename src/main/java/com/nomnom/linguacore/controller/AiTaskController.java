package com.nomnom.linguacore.controller;

import com.nomnom.linguacore.dto.response.AiTaskResponse;
import com.nomnom.linguacore.entity.Card;
import com.nomnom.linguacore.service.AiTaskService;
import com.nomnom.linguacore.service.CardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiTaskController {
    private final AiTaskService aiTaskService;
    private final CardService cardService;

    public AiTaskController(AiTaskService aiTaskService, CardService cardService) {
        this.aiTaskService = aiTaskService;
        this.cardService = cardService;
    }
    @PostMapping("/api/cards/{id}/examples")
    public ResponseEntity<AiTaskResponse> createExampleGenerationTask(@PathVariable Long id){
        Card card = cardService.findOwnedCard(id);
        AiTaskResponse ai = aiTaskService.createExampleGenerationTask(card);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ai);
    }
    @GetMapping("/api/tasks/{id}")
    public AiTaskResponse getTaskStatus(@PathVariable Long id){
        return aiTaskService.getTaskStatus(id);
    }
}
