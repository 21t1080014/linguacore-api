package com.nomnom.linguacore.dto.response;

public class UserResponse {
    private Long id;
    private String email;
    private String displayName;
    private String role;

    public UserResponse(Long id, String email, String displayName, String role) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.role = role;

    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRole() {
        return role;
    }
}
