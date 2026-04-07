package com.onlinelearning.gateway.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntrospectResponse {

    private boolean active;
    private Long userId;
    private String username;
    private String role;
    private String message;
}
