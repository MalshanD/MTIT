package com.onlinelearning.student.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    private Long studentId;  // Matches userId from Auth Service (NOT auto-generated)

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    private LocalDate dateOfBirth;

    @Column(length = 20)
    private String phoneNumber;

    private String profilePicture;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @Column(nullable = false)
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        this.registeredAt = LocalDateTime.now();
    }
}
