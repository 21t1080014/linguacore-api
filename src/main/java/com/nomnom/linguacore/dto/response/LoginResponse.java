package com.nomnom.linguacore.dto.response;

public class LoginResponse {
    private String token;
    private String email;
    private String displayName;

    public LoginResponse(String token, String email, String displayName) {
        this.token = token;
        this.email = email;
        this.displayName = displayName;
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }
}
