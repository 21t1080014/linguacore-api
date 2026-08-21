package com.nomnom.linguacore.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "decks")
public class Deck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 5)
    private String targetLang;
    @Column(columnDefinition = "TEXT")
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" , nullable = false)
    private User user;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Deck() {
    }

    public Deck(Long id, String name, String targetLang, String description, User user, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.targetLang = targetLang;
        this.description = description;
        this.user = user;
        this.createdAt = createdAt;
    }

    public Deck setName(String name) {
        this.name = name;
        return this;
    }

    public Deck setTargetLang(String targetLang) {
        this.targetLang = targetLang;
        return this;
    }

    public Deck setDescription(String description) {
        this.description = description;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Deck setUser(User user) {
        this.user = user;
        return this;
    }

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
