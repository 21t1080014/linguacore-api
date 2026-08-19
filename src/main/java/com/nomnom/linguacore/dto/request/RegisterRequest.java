package com.nomnom.linguacore.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    @Size(min = 6)
    private String password;
    private String displayName;

    public String getEmail() {
        return email;
    }

    public RegisterRequest setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public RegisterRequest setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getDisplayName() {
        return displayName;
    }

    public RegisterRequest setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }
}
