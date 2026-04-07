package com.onlinelearning.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
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
                        new Server().url("http://localhost:8081").description("Auth Service Direct")
                ))
                .info(new Info()
                        .title("Auth Service API")
                        .version("1.0.0")
                        .description("OAuth 2.0 Authentication Service for Online Learning Platform. "
                                + "Handles user registration, login, logout, and token introspection using opaque tokens.")
                        .contact(new Contact()
                                .name("Online Learning Platform Team")
                                .email("team@onlinelearning.com")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Token"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Token",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("opaque")
                                        .description("Enter your opaque access token")));
    }
}
