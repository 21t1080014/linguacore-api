package com.nomnom.linguacore.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "review_logs")
public class ReviewLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Short grade;
    @Column(length = 15, nullable = false)
    private String mode;
    @Column(name = "duration_ms")
    private Integer durationMs;
    @Column(name ="interval_before")
    private Integer intervalBefore;
    @Column(name = "interval_after")
    private Integer intervalAfter;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="card_id",nullable = false)
    private Card card;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant reviewedAt;

    public ReviewLog() {
        super();
    }

    public ReviewLog(Short grade, String mode, Integer durationMs, Integer intervalBefore, Integer intervalAfter, Card card) {
        this.grade = grade;
        this.mode = mode;
        this.durationMs = durationMs;
        this.intervalBefore = intervalBefore;
        this.intervalAfter = intervalAfter;
        this.card = card;
    }

    public Long getId() {
        return id;
    }

    public Short getGrade() {
        return grade;
    }

    public ReviewLog setGrade(Short grade) {
        this.grade = grade;
        return this;
    }

    public String getMode() {
        return mode;
    }

    public ReviewLog setMode(String mode) {
        this.mode = mode;
        return this;
    }

    public Integer getDurationMs() {
        return durationMs;
    }

    public ReviewLog setDurationMs(Integer durationMs) {
        this.durationMs = durationMs;
        return this;
    }

    public Integer getIntervalBefore() {
        return intervalBefore;
    }

    public ReviewLog setIntervalBefore(Integer intervalBefore) {
        this.intervalBefore = intervalBefore;
        return this;
    }

    public Integer getIntervalAfter() {
        return intervalAfter;
    }

    public ReviewLog setIntervalAfter(Integer intervalAfter) {
        this.intervalAfter = intervalAfter;
        return this;
    }

    public Card getCard() {
        return card;
    }

    public ReviewLog setCard(Card card) {
        this.card = card;
        return this;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }
}
