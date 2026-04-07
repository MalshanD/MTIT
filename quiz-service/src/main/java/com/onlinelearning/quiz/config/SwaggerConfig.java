package com.onlinelearning.quiz.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("API Gateway"),
                        new Server().url("http://localhost:8086").description("Quiz Service Direct")
                ))
                .info(new Info().title("Quiz Service API").version("1.0.0")
                        .description("Quiz, Question, and Attempt Management. Auto-grades quizzes and triggers enrollment completion."))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Token"))
                .components(new Components().addSecuritySchemes("Bearer Token",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("opaque")));
    }
}
