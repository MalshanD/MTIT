package com.onlinelearning.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IntrospectRequest {

    @NotBlank(message = "Token is required")
    private String token;
}
