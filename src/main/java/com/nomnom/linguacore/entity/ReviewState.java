package com.nomnom.linguacore.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "review_states",indexes = @Index(name = "idx_review_states_due_date", columnList = "due_date"))
public class ReviewState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(precision = 4, nullable = false,scale = 2)
    private BigDecimal easiness;
    private Integer repetitions;
    private Integer intervalDays;
    private LocalDate dueDate;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id",nullable = false,unique = true)
    private Card card;

    public ReviewState() {
        super();
    }

    public ReviewState(BigDecimal easiness, Integer repetitions, Integer intervalDays, LocalDate dueDate, Card card) {
        this.easiness = easiness;
        this.repetitions = repetitions;
        this.intervalDays = intervalDays;
        this.dueDate = dueDate;
        this.card = card;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getEasiness() {
        return easiness;
    }

    public ReviewState setEasiness(BigDecimal easiness) {
        this.easiness = easiness;
        return this;
    }

    public Integer getRepetitions() {
        return repetitions;
    }

    public ReviewState setRepetitions(Integer repetitions) {
        this.repetitions = repetitions;
        return this;
    }

    public Integer getIntervalDays() {
        return intervalDays;
    }

    public ReviewState setIntervalDays(Integer intervalDays) {
        this.intervalDays = intervalDays;
        return this;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public ReviewState setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    public Card getCard() {
        return card;
    }

    public ReviewState setCard(Card card) {
        this.card = card;
        return this;
    }
}
