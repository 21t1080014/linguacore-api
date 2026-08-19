package com.nomnom.linguacore.controller;

import com.nomnom.linguacore.dto.request.CreateReviewRequest;
import com.nomnom.linguacore.dto.response.CardResponse;
import com.nomnom.linguacore.dto.response.ReviewResultResponse;
import com.nomnom.linguacore.service.CardService;
import com.nomnom.linguacore.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final CardService cardService;
    private final ReviewService reviewService;

    public ReviewController(CardService cardService, ReviewService reviewService) {
        this.cardService = cardService;
        this.reviewService = reviewService;
    }

    @GetMapping("/due")
    public ResponseEntity<List<CardResponse>> getDueCards(){
        return ResponseEntity.ok(cardService.getDueCards());
    }
    @PostMapping
    public ResponseEntity<ReviewResultResponse> recordReview(@Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.ok(reviewService.recordReview(request));
    }
}
