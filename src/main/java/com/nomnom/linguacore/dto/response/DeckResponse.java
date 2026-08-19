package com.nomnom.linguacore.dto.response;

import java.time.Instant;

public class DeckResponse {
    // TODO 4: khai 5 trường — id, name, targetLang, description, createdAt
    private Long id;
    private String name;
    private String targetLang;
    private String description;
    private Instant createdAt;
    // TODO 5: một constructor nhận đủ 5 tham số (service sẽ dùng để đóng gói)

    public DeckResponse(Long id, String name, String tagertLang, String description, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.targetLang = tagertLang;
        this.description = description;
        this.createdAt = createdAt;
    }

    // TODO 6: getter cho cả 5 — và CHỈ getter. Tự hỏi: vì sao response không cần setter?

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTargetLang() {
        return targetLang;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
