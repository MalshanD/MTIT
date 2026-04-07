package com.onlinelearning.quiz.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SubmitAnswerRequest {
    @NotNull(message = "Quiz ID is required")
    private Long quizId;

    // Map of questionId -> selected option (A, B, C, D)
    @NotNull(message = "Answers are required")
    private Map<Long, String> answers;
}
