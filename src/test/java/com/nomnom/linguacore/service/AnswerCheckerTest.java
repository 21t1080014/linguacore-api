package com.nomnom.linguacore.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnswerCheckerTest {
    @Test
    void ignoresCaseAndOuterSpaces() {
        assertTrue(AnswerChecker.isCorrect("  Hello ", "hello"));
    }
    @Test
    void normalizesSpacesBetweenWords() {
        assertTrue(
                AnswerChecker.isCorrect("look   forward   to", "look forward to")
        );
    }
    @Test
    void normalizesFullWidthCharacters() {
        assertTrue(
                AnswerChecker.isCorrect("\uff48\uff45\uff4c\uff4c\uff4f", "hello")
        );
    }
    @Test
    void normalizesCombiningDiacritics() {
        assertTrue(
                AnswerChecker.isCorrect("cha\u0300o", "chào")
        );
    }
    @Test
    void normalizesCurlyApostrophe() {
        assertTrue(
                AnswerChecker.isCorrect("don\u2019t", "don't")
        );
    }
    @Test
    void rejectsIncorrectSpelling() {
        assertFalse(
                AnswerChecker.isCorrect("helo", "hello")
        );
    }
    @Test
    void rejectsMissingDiacritics() {
        assertFalse(
                AnswerChecker.isCorrect("chao", "chào")
        );
    }
}
