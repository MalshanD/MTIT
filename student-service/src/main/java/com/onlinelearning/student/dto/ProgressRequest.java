package com.onlinelearning.student.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProgressRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Completed lessons count is required")
    private Integer completedLessons;

    @NotNull(message = "Total lessons count is required")
    private Integer totalLessons;
}
