package com.onlinelearning.quiz.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuizResponse {
    private Long quizId;
    private Long courseId;
    private String title;
    private String description;
    private Integer totalMarks;
    private Integer passingMarks;
    private Integer timeLimit;
    private Boolean isActive;
}
