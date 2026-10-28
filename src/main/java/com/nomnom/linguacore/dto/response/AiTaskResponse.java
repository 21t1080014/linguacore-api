package com.nomnom.linguacore.dto.response;

import java.time.Instant;

public class AiTaskResponse {

    private Long id;
    private String taskType;
    private String status;
    private Long cardId;
    private String result;
    private Instant createdAt;
    private Instant updatedAt;

    public AiTaskResponse(
            Long id,
            String taskType,
            String status,
            Long cardId,
            String result,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.taskType = taskType;
        this.status = status;
        this.cardId = cardId;
        this.result = result;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTaskType() {
        return taskType;
    }

    public String getStatus() {
        return status;
    }

    public Long getCardId() {
        return cardId;
    }

    public String getResult() {
        return result;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
