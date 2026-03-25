package com.onlinelearning.auth.controller;

import com.onlinelearning.auth.dto.*;
import com.onlinelearning.auth.service.impl.AuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Service", description = "OAuth 2.0 Authentication - Register, Login, Logout, Token Introspection")
public class AuthController {

    private final AuthServiceImpl authServiceImpl;

    // ==================== REGISTER ====================

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new STUDENT or INSTRUCTOR account and returns an access token")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authServiceImpl.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ==================== LOGIN ====================

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates the user and returns an opaque access token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authServiceImpl.login(request);
        return ResponseEntity.ok(response);
    }

    // ==================== LOGOUT ====================

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revokes the access token so it can no longer be used")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        String message = authServiceImpl.logout(token);
        return ResponseEntity.ok(message);
    }

    // ==================== INTROSPECT ====================
    // This endpoint is called by the API Gateway to validate tokens

    @PostMapping("/introspect")
    @Operation(summary = "Introspect token", description = "Called by API Gateway to validate a token and get user details")
    public ResponseEntity<IntrospectResponse> introspect(@Valid @RequestBody IntrospectRequest request) {
        IntrospectResponse response = authServiceImpl.introspect(request);
        return ResponseEntity.ok(response);
    }

    // ==================== GET PROFILE ====================

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", description = "Returns the profile of the currently logged-in user based on the token")
    public ResponseEntity<ProfileResponse> getProfile(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        ProfileResponse response = authServiceImpl.getProfile(token);
        return ResponseEntity.ok(response);
    }

    // ==================== HELPER ====================

    /**
     * Extracts the token value from the Authorization header.
     * Expected format: "Bearer <token>"
     */
    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header. Expected: Bearer <token>");
        }
        return authHeader.substring(7);
    }
}
