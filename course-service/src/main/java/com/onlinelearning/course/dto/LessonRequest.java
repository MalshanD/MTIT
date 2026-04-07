package com.onlinelearning.course.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonRequest {

    @NotBlank(message = "Lesson title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    private String content;

    private String videoUrl;

    private Integer duration;  // in minutes

    @NotNull(message = "Order index is required")
    private Integer orderIndex;

    private Boolean isPreview = false;
}
