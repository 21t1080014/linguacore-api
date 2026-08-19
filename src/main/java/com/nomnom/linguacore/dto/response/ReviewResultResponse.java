package com.nomnom.linguacore.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReviewResultResponse {
    private LocalDate dueDate;
    private Integer intervalDays;
    private Integer repetitions;
    private BigDecimal easiness;

    public ReviewResultResponse(LocalDate dueDate, Integer intervalDays, Integer repetitions, BigDecimal easiness) {
        this.dueDate = dueDate;
        this.intervalDays = intervalDays;
        this.repetitions = repetitions;
        this.easiness = easiness;
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
