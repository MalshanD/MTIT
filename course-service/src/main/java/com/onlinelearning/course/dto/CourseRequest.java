package com.onlinelearning.course.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequest {

    @NotBlank(message = "Course title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    private String description;

    private Long categoryId;

    private String thumbnailUrl;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private Double price;

    @NotBlank(message = "Level is required")
    @Pattern(regexp = "BEGINNER|INTERMEDIATE|ADVANCED", message = "Level must be BEGINNER, INTERMEDIATE, or ADVANCED")
    private String level;

    private String language;
}
