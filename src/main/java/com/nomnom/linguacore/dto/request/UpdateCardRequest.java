package com.nomnom.linguacore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateCardRequest {
    @NotBlank
    private String front;
    @NotBlank
    private String back;
    @NotBlank
    @Size(max = 20)
    private String pos;
    private String note;

    public String getFront() {
        return front;
    }

    public UpdateCardRequest setFront(String front) {
        this.front = front;
        return this;
    }

    public String getBack() {
        return back;
    }

    public UpdateCardRequest setBack(String back) {
        this.back = back;
        return this;
    }

    public String getPos() {
        return pos;
    }

    public UpdateCardRequest setPos(String pos) {
        this.pos = pos;
        return this;
    }

    public String getNote() {
        return note;
    }

    public UpdateCardRequest setNote(String note) {
        this.note = note;
        return this;
    }
}
