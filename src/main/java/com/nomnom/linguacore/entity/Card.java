package com.nomnom.linguacore.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "TEXT" , nullable = false)
    private String front;
    @Column(columnDefinition = "TEXT",nullable = false)
    private String back;
    @Column(length = 20)
    private String pos;
    @Column(columnDefinition = "TEXT")
    private String note;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id" , nullable = false)
    private Deck deck;

    public Card() {
    }

    public Card(String front, String back, String pos, String note, Deck deck) {
        this.front = front;
        this.back = back;
        this.pos = pos;
        this.note = note;
        this.deck = deck;
    }

    public Long getId() {
        return id;
    }

    public String getFront() {
        return front;
    }

    public Card setFront(String front) {
        this.front = front;
        return this;
    }

    public String getBack() {
        return back;
    }

    public Card setBack(String back) {
        this.back = back;
        return this;
    }

    public String getPos() {
        return pos;
    }

    public Card setPos(String pos) {
        this.pos = pos;
        return this;
    }

    public String getNote() {
        return note;
    }

    public Card setNote(String note) {
        this.note = note;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Deck getDeck() {
        return deck;
    }

    public Card setDeck(Deck deck) {
        this.deck = deck;
        return this;
    }
}
