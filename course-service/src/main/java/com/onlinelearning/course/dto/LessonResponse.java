package com.onlinelearning.course.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonResponse {

    private Long lessonId;
    private Long courseId;
    private String title;
    private String content;
    private String videoUrl;
    private Integer duration;
    private Integer orderIndex;
    private Boolean isPreview;
}
