package com.onlinelearning.auth.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QuizAttemptDto {
    private Long attemptId;
    private Long quizId;
    private Long studentId;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private Integer score;
    private Boolean isPassed;
    private Integer attemptNumber;
}
