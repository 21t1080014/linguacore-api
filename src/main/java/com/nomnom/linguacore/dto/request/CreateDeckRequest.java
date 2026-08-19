package com.nomnom.linguacore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateDeckRequest {
    @NotBlank(message = "Tên bộ thẻ không được trống")
    @Size(max = 100)
    private String name;
    @NotBlank
    @Size(max = 5)
    private String targetLang;
    private String description;

    public String getName() {
        return name;
    }

    public CreateDeckRequest setName(String name) {
        this.name = name;
        return this;
    }

    public String getTargetLang() {
        return targetLang;
    }

    public CreateDeckRequest setTargetLang(String targetLang) {
        this.targetLang = targetLang;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public CreateDeckRequest setDescription(String description) {
        this.description = description;
        return this;
    }
}
