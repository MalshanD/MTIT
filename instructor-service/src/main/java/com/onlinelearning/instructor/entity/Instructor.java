package com.onlinelearning.instructor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "instructors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Instructor {

    @Id
    private Long instructorId;  // Matches userId from Auth Service

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 100)
    private String expertise;

    private String profilePicture;

    private String linkedinProfile;

    private String websiteUrl;

    @Column(nullable = false)
    private Integer totalCourses = 0;

    @Column(nullable = false)
    private Integer totalStudents = 0;

    @Column(nullable = false)
    private Double rating = 0.0;

    @Column(nullable = false)
    private Boolean isVerified = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        this.joinedAt = LocalDateTime.now();
    }
}
