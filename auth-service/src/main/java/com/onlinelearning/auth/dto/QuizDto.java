package com.onlinelearning.auth.dto;

import lombok.Data;

@Data
public class QuizDto {
    private Long quizId;
    private Long courseId;
    private String title;
    private String description;
    private Integer totalMarks;
    private Integer passingMarks;
    private Integer timeLimit;
    private Boolean isActive;
}
