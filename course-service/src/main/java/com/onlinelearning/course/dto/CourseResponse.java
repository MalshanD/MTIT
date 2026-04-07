package com.onlinelearning.course.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {

    private Long courseId;
    private String title;
    private String description;
    private Long instructorId;
    private Long categoryId;
    private String categoryName;
    private String thumbnailUrl;
    private Double price;
    private String level;
    private String language;
    private Boolean isPublished;
    private Integer totalLessons;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
