package com.onlinelearning.auth.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {

    private Long userId;
    private String username;
    private String email;
    private String role;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
