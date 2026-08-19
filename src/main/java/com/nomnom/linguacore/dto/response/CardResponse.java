package com.nomnom.linguacore.dto.response;

import java.time.Instant;

public class CardResponse {
    private Long id;
    private String front;
    private String back;
    private String pos;
    private String note;
    private Instant createdAt;
    private Long deckId;

    public CardResponse(Long id, String front, String back, String pos, String note, Instant createdAt, Long deckId) {
        this.id = id;
        this.front = front;
        this.back = back;
        this.pos = pos;
        this.note = note;
        this.createdAt = createdAt;
        this.deckId = deckId;
    }

    public Long getId() {
        return id;
    }

    public String getFront() {
        return front;
    }

    public String getBack() {
        return back;
    }

    public String getPos() {
        return pos;
    }

    public String getNote() {
        return note;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getDeckId() {
        return deckId;
    }
}
