package com.onlinelearning.certificate.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CertificateResponse {
    private Long certificateId;
    private String certificateCode;
    private Long studentId;
    private Long courseId;
    private Long instructorId;
    private String studentName;
    private String courseName;
    private LocalDateTime issuedAt;
    private String certificateUrl;
    private Boolean isValid;
}
