package com.onlinelearning.enrollment.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishListResponse {

    private Long wishlistId;
    private Long studentId;
    private Long courseId;
    private LocalDateTime addedAt;
}
