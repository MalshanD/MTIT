package com.onlinelearning.instructor.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Long reviewId;
    private Long instructorId;
    private Long studentId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
