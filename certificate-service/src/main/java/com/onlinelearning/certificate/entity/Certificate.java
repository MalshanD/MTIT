package com.onlinelearning.certificate.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "certificates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long certificateId;

    @Column(nullable = false, unique = true, length = 50)
    private String certificateCode;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private Long instructorId;

    @Column(nullable = false)
    private String studentName;

    @Column(nullable = false)
    private String courseName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    private String certificateUrl;

    @Column(nullable = false)
    private Boolean isValid = true;

    @PrePersist
    protected void onCreate() {
        this.issuedAt = LocalDateTime.now();
    }
}
