package com.nomnom.linguacore.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateReviewRequest {
    @NotNull
    private Long cardId;
    @Min(0)
    @Max(5)
    @NotNull
    private Integer grade;
    @NotBlank
    private  String mode;
    private  Integer durationMs;

    public Long getCardId() {
        return cardId;
    }

    public CreateReviewRequest setCardId(Long cardId) {
        this.cardId = cardId;
        return this;
    }

    public Integer getGrade() {
        return grade;
    }

    public CreateReviewRequest setGrade(Integer grade) {
        this.grade = grade;
        return this;
    }

    public String getMode() {
        return mode;
    }

    public CreateReviewRequest setMode(String mode) {
        this.mode = mode;
        return this;
    }

    public Integer getDurationMs() {
        return durationMs;
    }

    public CreateReviewRequest setDurationMs(Integer durationMs) {
        this.durationMs = durationMs;
        return this;
    }
}
