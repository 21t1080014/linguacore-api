package com.nomnom.linguacore.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true,nullable = false)
    private String email;
    @Column(nullable = false)
    private String passwordHash;
    @Column
    private String displayName;
    @Column(length = 10,nullable = false,columnDefinition = "VARCHAR(10) DEFAULT 'user'")
    private String role;
    @Column(length = 5)
    private String nativeLang;
    @Column(length = 10)
    private String learningMode;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public User() {
    }

    public User( String email, String passwordHash, String displayName, String role, String nativeLang, String learningMode) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.role = role;
        this.nativeLang = nativeLang;
        this.learningMode = learningMode;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public User setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public String getDisplayName() {
        return displayName;
    }

    public User setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String getRole() {
        return role;
    }

    public User setRole(String role) {
        this.role = role;
        return this;
    }

    public String getNativeLang() {
        return nativeLang;
    }

    public User setNativeLang(String nativeLang) {
        this.nativeLang = nativeLang;
        return this;
    }

    public String getLearningMode() {
        return learningMode;
    }

    public User setLearningMode(String learningMode) {
        this.learningMode = learningMode;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
