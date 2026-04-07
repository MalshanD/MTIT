package com.onlinelearning.enrollment.controller;

import com.onlinelearning.enrollment.dto.*;
import com.onlinelearning.enrollment.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollment Service", description = "Manage student enrollments and wishlists")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

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
                    "Access denied. You can only access your own enrollments.");
        }
    }

    // ==================== ENROLLMENT ENDPOINTS ====================

    @PostMapping("/enroll")
    @Operation(summary = "Enroll student in a course",
            description = "Only STUDENT role can enroll in courses.")
    public ResponseEntity<EnrollmentResponse> enroll(
            @RequestHeader("X-User-Id") Long studentId,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody EnrollRequest request) {
        requireRole(userRole, "STUDENT");
        EnrollmentResponse response = enrollmentService.enrollStudent(studentId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get all enrollments for a student",
            description = "Students can view own enrollments. Instructors and Admins can view any.")
    public ResponseEntity<List<EnrollmentResponse>> getByStudent(
            @PathVariable Long studentId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {
        if ("STUDENT".equalsIgnoreCase(userRole)) {
            requireOwnershipOrAdmin(userId, studentId, userRole);
        }
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudent(studentId));
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get all enrollments for a course",
            description = "Only INSTRUCTOR or ADMIN can view enrolled students for a course.")
    public ResponseEntity<List<EnrollmentResponse>> getByCourse(
            @PathVariable Long courseId,
            @RequestHeader("X-User-Role") String userRole) {
        requireRole(userRole, "INSTRUCTOR", "ADMIN");
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourse(courseId));
    }

    @PutMapping("/complete")
    @Operation(summary = "Mark enrollment as completed",
            description = "Only ADMIN or internal service calls (Quiz Service) can mark completion. Triggers certificate issuance.")
    public ResponseEntity<EnrollmentResponse> markCompleted(
            @RequestParam Long studentId,
            @RequestParam Long courseId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        // Allow ADMIN or inter-service calls (Quiz Service calls this without role header)
        if (userRole != null && !("ADMIN".equalsIgnoreCase(userRole) || "INSTRUCTOR".equalsIgnoreCase(userRole))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied. Only ADMIN or system can mark enrollment as completed.");
        }
        return ResponseEntity.ok(enrollmentService.markAsCompleted(studentId, courseId));
    }

    @PutMapping("/drop")
    @Operation(summary = "Drop a course enrollment",
            description = "Students can only drop their own enrollments. Admins can drop any.")
    public ResponseEntity<EnrollmentResponse> drop(
            @RequestHeader("X-User-Id") Long studentId,
            @RequestHeader("X-User-Role") String userRole,
            @RequestParam Long courseId) {
        requireRole(userRole, "STUDENT", "ADMIN");
        return ResponseEntity.ok(enrollmentService.dropEnrollment(studentId, courseId));
    }

    @GetMapping("/check")
    @Operation(summary = "Check if student is enrolled in a course")
    public ResponseEntity<Map<String, Boolean>> checkEnrolled(
            @RequestParam Long studentId,
            @RequestParam Long courseId) {
        return ResponseEntity.ok(Map.of("enrolled", enrollmentService.isEnrolled(studentId, courseId)));
    }

    // ==================== WISHLIST ====================

    @PostMapping("/wishlist")
    @Operation(summary = "Add course to wishlist",
            description = "Only STUDENT role can manage wishlists.")
    public ResponseEntity<WishListResponse> addToWishList(
            @RequestHeader("X-User-Id") Long studentId,
            @RequestHeader("X-User-Role") String userRole,
            @RequestParam Long courseId) {
        requireRole(userRole, "STUDENT");
        return new ResponseEntity<>(enrollmentService.addToWishList(studentId, courseId), HttpStatus.CREATED);
    }

    @GetMapping("/wishlist")
    @Operation(summary = "Get student's wishlist",
            description = "Only STUDENT role. Returns only own wishlist.")
    public ResponseEntity<List<WishListResponse>> getWishList(
            @RequestHeader("X-User-Id") Long studentId,
            @RequestHeader("X-User-Role") String userRole) {
        requireRole(userRole, "STUDENT", "ADMIN");
        return ResponseEntity.ok(enrollmentService.getWishList(studentId));
    }

    @DeleteMapping("/wishlist")
    @Operation(summary = "Remove course from wishlist",
            description = "Only STUDENT role can remove from own wishlist.")
    public ResponseEntity<String> removeFromWishList(
            @RequestHeader("X-User-Id") Long studentId,
            @RequestHeader("X-User-Role") String userRole,
            @RequestParam Long courseId) {
        requireRole(userRole, "STUDENT", "ADMIN");
        return ResponseEntity.ok(enrollmentService.removeFromWishList(studentId, courseId));
    }
}
