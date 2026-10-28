package com.nomnom.linguacore.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReviewResultResponse {
    private LocalDate dueDate;
    private Integer intervalDays;
    private Integer repetitions;
    private BigDecimal easiness;
    private Boolean correct;       // null ở chế độ tự chấm
    private String correctAnswer;  // null ở chế độ tự chấm

    public ReviewResultResponse(LocalDate dueDate, Integer intervalDays, Integer repetitions, BigDecimal easiness, Boolean correct, String correctAnswer) {
        this.dueDate = dueDate;
        this.intervalDays = intervalDays;
        this.repetitions = repetitions;
        this.easiness = easiness;
        this.correct = correct;
        this.correctAnswer = correctAnswer;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public BigDecimal getEasiness() {
        return easiness;
    }

    public Integer getRepetitions() {
        return repetitions;
    }

    public Integer getIntervalDays() {
        return intervalDays;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }
}
