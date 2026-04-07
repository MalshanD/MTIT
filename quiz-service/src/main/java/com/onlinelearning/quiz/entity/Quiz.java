package com.onlinelearning.quiz.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quizzes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quizId;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer totalMarks = 0;

    @Column(nullable = false)
    private Integer passingMarks = 0;

    @Column(nullable = false)
    private Integer timeLimit = 0;  // minutes, 0 = no limit

    @Column(nullable = false)
    private Boolean isActive = true;
}
