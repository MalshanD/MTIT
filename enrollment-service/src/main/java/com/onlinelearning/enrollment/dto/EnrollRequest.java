package com.onlinelearning.enrollment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    private String paymentId;
    private Double amountPaid;
}
