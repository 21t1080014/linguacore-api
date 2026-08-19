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
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Deck() {
    }

    public Deck(String name, String targetLang, String description) {
        this.description = description;
        this.targetLang = targetLang;
        this.name = name;
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
