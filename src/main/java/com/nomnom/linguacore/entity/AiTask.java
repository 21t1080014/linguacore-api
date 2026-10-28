package com.nomnom.linguacore.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
@Entity
public class AiTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 30,nullable = false)
    private String taskType;
    @Column(length = 15,nullable = false)
    private String status;
    @ManyToOne
    @JoinColumn(name = "card_id")
    private Card card;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(columnDefinition = "TEXT")
    private String inputText;
    @Column(columnDefinition = "TEXT")
    private String result;
    private Integer retryCount;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public AiTask() {
    }

    public AiTask(Long id, String taskType, String status, Card card, User user, String inputText, String result, Integer retryCount, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.taskType = taskType;
        this.status = status;
        this.card = card;
        this.user = user;
        this.inputText = inputText;
        this.result = result;
        this.retryCount = retryCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public AiTask setId(Long id) {
        this.id = id;
        return this;
    }

    public String getTaskType() {
        return taskType;
    }

    public AiTask setTaskType(String taskType) {
        this.taskType = taskType;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public AiTask setStatus(String status) {
        this.status = status;
        return this;
    }

    public Card getCard() {
        return card;
    }

    public AiTask setCard(Card card) {
        this.card = card;
        return this;
    }

    public User getUser() {
        return user;
    }

    public AiTask setUser(User user) {
        this.user = user;
        return this;
    }

    public String getInputText() {
        return inputText;
    }

    public AiTask setInputText(String inputText) {
        this.inputText = inputText;
        return this;
    }

    public String getResult() {
        return result;
    }

    public AiTask setResult(String result) {
        this.result = result;
        return this;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public AiTask setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
        return this;
    }
}
