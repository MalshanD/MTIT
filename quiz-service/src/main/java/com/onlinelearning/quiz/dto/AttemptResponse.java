package com.onlinelearning.quiz.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AttemptResponse {
    private Long attemptId;
    private Long quizId;
    private Long studentId;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private Integer score;
    private Boolean isPassed;
    private Integer attemptNumber;
}
