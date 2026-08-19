package com.nomnom.linguacore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCardRequest {
    @NotBlank
    private String front;
    @NotBlank
    private String back;
    @NotBlank
    @Size(max = 20)
    private String pos;
    private String note;

    public String getNote() {
        return note;
    }

    public CreateCardRequest setNote(String note) {
        this.note = note;
        return this;
    }

    public String getPos() {
        return pos;
    }

    public CreateCardRequest setPos(String pos) {
        this.pos = pos;
        return this;
    }

    public String getBack() {
        return back;
    }

    public CreateCardRequest setBack(String back) {
        this.back = back;
        return this;
    }

    public String getFront() {
        return front;
    }

    public CreateCardRequest setFront(String front) {
        this.front = front;
        return this;
    }
}
