package com.onlinelearning.auth.service;

import com.onlinelearning.auth.dto.*;
import com.onlinelearning.auth.entity.*;
import com.onlinelearning.auth.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final PasswordEncoder passwordEncoder;

    // Token validity duration: 24 hours
    private static final int TOKEN_EXPIRY_HOURS = 24;

    // ==================== REGISTER ====================

    public AuthResponse register(RegisterRequest request) {

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username '" + request.getUsername() + "' is already taken");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email '" + request.getEmail() + "' is already registered");
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.valueOf(request.getRole()))
                .isActive(true)
                .build();

        user = userRepository.save(user);

        // Generate access token for the newly registered user
        String tokenValue = generateOpaqueToken();
        AccessToken accessToken = AccessToken.builder()
                .tokenValue(tokenValue)
                .userId(user.getUserId())
                .userRole(user.getRole())
                .expiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS))
                .isRevoked(false)
                .build();

        accessTokenRepository.save(accessToken);

        return AuthResponse.builder()
                .accessToken(tokenValue)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .message("Registration successful")
                .build();
    }

    // ==================== LOGIN ====================

    public AuthResponse login(LoginRequest request) {

        // Find user by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        // Check if account is active
        if (!user.getIsActive()) {
            throw new RuntimeException("Account is deactivated. Please contact support.");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }

        // Generate new opaque access token
        String tokenValue = generateOpaqueToken();
        AccessToken accessToken = AccessToken.builder()
                .tokenValue(tokenValue)
                .userId(user.getUserId())
                .userRole(user.getRole())
                .expiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS))
                .isRevoked(false)
                .build();

        accessTokenRepository.save(accessToken);

        return AuthResponse.builder()
                .accessToken(tokenValue)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }

    // ==================== LOGOUT ====================

    @Transactional
    public String logout(String tokenValue) {

        AccessToken token = accessTokenRepository.findByTokenValue(tokenValue)
                .orElseThrow(() -> new RuntimeException("Token not found or already invalidated"));

        // Revoke the token
        token.setIsRevoked(true);
        accessTokenRepository.save(token);

        return "Logout successful. Token has been revoked.";
    }

    // ==================== INTROSPECT ====================
    // Called by the API Gateway to validate tokens

    public IntrospectResponse introspect(IntrospectRequest request) {

        // Find the token in the database
        AccessToken token = accessTokenRepository.findByTokenValue(request.getToken())
                .orElse(null);

        // Token not found
        if (token == null) {
            return IntrospectResponse.builder()
                    .active(false)
                    .message("Token not found")
                    .build();
        }

        // Token is revoked (logged out)
        if (token.getIsRevoked()) {
            return IntrospectResponse.builder()
                    .active(false)
                    .message("Token has been revoked")
                    .build();
        }

        // Token is expired
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            return IntrospectResponse.builder()
                    .active(false)
                    .message("Token has expired")
                    .build();
        }

        // Token is valid - fetch the user details
        User user = userRepository.findById(token.getUserId())
                .orElse(null);

        if (user == null || !user.getIsActive()) {
            return IntrospectResponse.builder()
                    .active(false)
                    .message("User not found or account is deactivated")
                    .build();
        }

        return IntrospectResponse.builder()
                .active(true)
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .message("Token is valid")
                .build();
    }

    // ==================== GET PROFILE ====================

    public ProfileResponse getProfile(String tokenValue) {

        // Validate token first
        AccessToken token = accessTokenRepository.findByTokenValue(tokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (token.getIsRevoked() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token is revoked or expired. Please login again.");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ProfileResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    // ==================== HELPER METHODS ====================

    /**
     * Generates a random opaque token (NOT JWT).
     * This is a UUID-based random string that has no embedded data.
     */
    private String generateOpaqueToken() {
        return UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
    }
}
