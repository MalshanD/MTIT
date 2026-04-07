package com.onlinelearning.quiz.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class QuizRequest {
    @NotNull(message = "Course ID is required") private Long courseId;
    @NotBlank(message = "Title is required") private String title;
    private String description;
    @NotNull(message = "Total marks is required") private Integer totalMarks;
    @NotNull(message = "Passing marks is required") private Integer passingMarks;
    private Integer timeLimit;
}
