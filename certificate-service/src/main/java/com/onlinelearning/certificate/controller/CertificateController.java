package com.onlinelearning.certificate.controller;

import com.onlinelearning.certificate.dto.CertificateResponse;
import com.onlinelearning.certificate.service.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
@Tag(name = "Certificate Service", description = "Certificate generation and verification")
public class CertificateController {

    private final CertificateService certificateService;

    // ==================== ROLE & OWNERSHIP HELPERS ====================

    private void requireRole(String role, String... allowed) {
        for (String r : allowed) {
            if (r.equalsIgnoreCase(role)) return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Access denied. Required role: " + String.join(" or ", allowed));
    }

    private void requireOwnershipOrAdmin(Long userId, Long resourceOwnerId, String role) {
        if ("ADMIN".equalsIgnoreCase(role)) return;
        if (!userId.equals(resourceOwnerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied. You can only view your own certificates.");
        }
    }

    // ==================== ENDPOINTS ====================

    @PostMapping("/issue")
    @Operation(summary = "Issue a certificate",
            description = "Only ADMIN or internal service calls (Enrollment Service) can issue certificates.")
    public ResponseEntity<CertificateResponse> issueCertificate(
            @RequestParam Long studentId,
            @RequestParam Long courseId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        // Allow ADMIN or inter-service calls (Enrollment Service calls this without explicit role check)
        if (userRole != null && !("ADMIN".equalsIgnoreCase(userRole) || "INSTRUCTOR".equalsIgnoreCase(userRole))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied. Only ADMIN or system can issue certificates.");
        }
        return new ResponseEntity<>(certificateService.issueCertificate(studentId, courseId), HttpStatus.CREATED);
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get all certificates for a student",
            description = "Students can view own certificates. Instructors and Admins can view any.")
    public ResponseEntity<List<CertificateResponse>> getByStudent(
            @PathVariable Long studentId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {
        if ("STUDENT".equalsIgnoreCase(userRole)) {
            requireOwnershipOrAdmin(userId, studentId, userRole);
        }
        return ResponseEntity.ok(certificateService.getCertificatesByStudent(studentId));
    }

    @GetMapping("/verify/{certificateCode}")
    @Operation(summary = "Verify a certificate by its code",
            description = "Any authenticated user can verify certificate authenticity.")
    public ResponseEntity<CertificateResponse> verify(@PathVariable String certificateCode) {
        return ResponseEntity.ok(certificateService.verifyCertificate(certificateCode));
    }

    @PutMapping("/{certificateId}/revoke")
    @Operation(summary = "Revoke a certificate",
            description = "Only ADMIN can revoke certificates.")
    public ResponseEntity<String> revoke(
            @PathVariable Long certificateId,
            @RequestHeader("X-User-Role") String userRole) {
        requireRole(userRole, "ADMIN");
        return ResponseEntity.ok(certificateService.revokeCertificate(certificateId));
    }
}
