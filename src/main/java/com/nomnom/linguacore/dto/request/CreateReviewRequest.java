package com.nomnom.linguacore.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nomnom.linguacore.enums.AnswerType;
import com.nomnom.linguacore.enums.ReviewMode;
import jakarta.validation.constraints.*;

public class CreateReviewRequest {
    @NotNull
    private Long cardId;
    @Min(0)
    @Max(5)
    private Integer grade;
    @Size(max = 500)
    private String typedAnswer;
    @NotNull
    private ReviewMode mode;
    @PositiveOrZero          // chặn số âm
    @Max(3_600_000)          // tối đa 1 giờ cho một thẻ; lớn hơn là dữ liệu rác làm lệch thống kê
    private Integer durationMs;


    @JsonIgnore
    @AssertTrue(message = "Chế độ tự chấm cần grade, chế độ gõ cần typedAnswer không rỗng")
    public boolean isGradeOrAnswerPresent() {
        if (mode == null) return true;   // lỗi thiếu mode đã do @NotNull báo
        return mode.getAnswerType() == AnswerType.TYPED ? typedAnswer != null && !typedAnswer.isBlank() : grade != null;
    }
    public Long getCardId() {
        return cardId;
    }

    public String getTypedAnswer() {
        return typedAnswer;
    }

    public CreateReviewRequest setTypedAnswer(String typedAnswer) {
        this.typedAnswer = typedAnswer;
        return this;
    }

    public CreateReviewRequest setCardId(Long cardId) {
        this.cardId = cardId;
        return this;
    }

    public Integer getGrade() {
        return grade;
    }

    public CreateReviewRequest setGrade(Integer grade) {
        this.grade = grade;
        return this;
    }

    public ReviewMode getMode() {
        return mode;
    }

    public CreateReviewRequest setMode(ReviewMode mode) {
        this.mode = mode;
        return this;
    }

    public Integer getDurationMs() {
        return durationMs;
    }

    public CreateReviewRequest setDurationMs(Integer durationMs) {
        this.durationMs = durationMs;
        return this;
    }
}
