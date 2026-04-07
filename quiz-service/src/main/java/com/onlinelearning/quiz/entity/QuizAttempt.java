package com.onlinelearning.quiz.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_attempts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attemptId;

    @Column(nullable = false)
    private Long quizId;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime submittedAt;

    @Column(nullable = false)
    private Integer score = 0;

    @Column(nullable = false)
    private Boolean isPassed = false;

    @Column(nullable = false)
    private Integer attemptNumber = 1;

    @PrePersist
    protected void onCreate() {
        this.startedAt = LocalDateTime.now();
    }
}
