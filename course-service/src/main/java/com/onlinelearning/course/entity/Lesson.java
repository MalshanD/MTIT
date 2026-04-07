package com.onlinelearning.course.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lessonId;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String videoUrl;

    private Integer duration;  // in minutes

    @Column(nullable = false)
    private Integer orderIndex = 0;

    @Column(nullable = false)
    private Boolean isPreview = false;
}
