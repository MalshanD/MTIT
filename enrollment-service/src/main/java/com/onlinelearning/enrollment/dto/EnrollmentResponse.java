package com.onlinelearning.enrollment.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentResponse {

    private Long enrollmentId;
    private Long studentId;
    private Long courseId;
    private LocalDateTime enrolledAt;
    private String status;
    private LocalDateTime completedAt;
    private String paymentId;
    private Double amountPaid;
}
