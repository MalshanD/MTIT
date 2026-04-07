package com.onlinelearning.student.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long progressId;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long courseId;  // ID from Course Service

    @Column(nullable = false)
    private Integer completedLessons = 0;

    @Column(nullable = false)
    private Integer totalLessons = 0;

    @Column(nullable = false)
    private Double progressPercent = 0.0;

    private LocalDateTime lastAccessedAt;

    @Column(nullable = false)
    private Boolean isCompleted = false;

    @PreUpdate
    protected void onUpdate() {
        this.lastAccessedAt = LocalDateTime.now();
        if (totalLessons > 0) {
            this.progressPercent = ((double) completedLessons / totalLessons) * 100;
        }
        if (this.progressPercent >= 100.0) {
            this.isCompleted = true;
        }
    }
}
