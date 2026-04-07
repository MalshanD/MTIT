package com.onlinelearning.gateway.filter;

import com.onlinelearning.gateway.dto.IntrospectResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final WebClient webClient;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    // Endpoints that do NOT require a token
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/introspect",
            // Swagger UI & OpenAPI docs (Gateway + all services)
            "/swagger-ui",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/webjars/swagger-ui",
            "/services/"
    );

    public AuthenticationFilter(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        // Get the Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        // Call Auth Service /introspect to validate the token
        return webClient.post()
                .uri(authServiceUrl + "/api/auth/introspect")
                .bodyValue(Map.of("token", token))
                .retrieve()
                .bodyToMono(IntrospectResponse.class)
                .flatMap(introspectResponse -> {
                    if (!introspectResponse.isActive()) {
                        return onError(exchange, "Token is invalid or expired", HttpStatus.UNAUTHORIZED);
                    }

                    // Token is valid - attach user details to the request headers
                    // so downstream services know who the user is
                    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", String.valueOf(introspectResponse.getUserId()))
                            .header("X-Username", introspectResponse.getUsername())
                            .header("X-User-Role", introspectResponse.getRole())
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                })
                .onErrorResume(error -> {
                    return onError(exchange, "Auth Service unavailable", HttpStatus.SERVICE_UNAVAILABLE);
                });
    }

    @Override
    public int getOrder() {
        return -1; // Run this filter before all others
    }

    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        byte[] bytes = ("{\"error\": \"" + message + "\"}").getBytes();
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }
}
