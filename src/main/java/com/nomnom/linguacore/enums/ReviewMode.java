package com.nomnom.linguacore.enums;

public enum ReviewMode {
    // Mỗi hằng số là MỘT ĐỐI TƯỢNG, được tạo bằng constructor với tham số trong ngoặc
    FLASHCARD(AnswerType.SELF),   // mặc định theo SAD: hiện front, lật xem back
    REVERSE(AnswerType.SELF),     // hiện back, tự nhớ front
    TYPING(AnswerType.TYPED);     // hiện back, người học gõ front

    private final AnswerType answerType;

    ReviewMode(AnswerType answerType) { this.answerType = answerType; }

    public AnswerType getAnswerType() { return answerType; }
}
