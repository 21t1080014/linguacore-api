package com.nomnom.linguacore.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class AnswerChecker {
    // Biên dịch regex MỘT LẦN; viết "\\s+" ngay trong replaceAll thì mỗi lần gọi lại biên dịch
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private AnswerChecker() {}   // lớp tiện ích, không cho new

    public static boolean isCorrect(String typed, String expected) {
        return normalize(typed).equals(normalize(expected));
    }

    private static String normalize(String text) {
        String s = Normalizer.normalize(text, Normalizer.Form.NFKC) // (1)
                .replace('\u2019', '\'')                            // (2)
                .strip()                                            // (3)
                .toLowerCase(Locale.ROOT);                          // (4)
        return WHITESPACE.matcher(s).replaceAll(" ");              // (5)
    }
}
