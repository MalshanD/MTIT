package com.onlinelearning.student.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressResponse {

    private Long progressId;
    private Long studentId;
    private Long courseId;
    private Integer completedLessons;
    private Integer totalLessons;
    private Double progressPercent;
    private LocalDateTime lastAccessedAt;
    private Boolean isCompleted;
}
