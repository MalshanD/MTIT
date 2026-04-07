package com.onlinelearning.instructor.controller;

import com.onlinelearning.instructor.dto.*;
import com.onlinelearning.instructor.service.InstructorService;
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
@RequestMapping("/api/instructors")
@RequiredArgsConstructor
@Tag(name = "Instructor Service", description = "Manage instructor profiles and reviews")
public class InstructorController {

    private final InstructorService instructorService;

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
                    "Access denied. You can only modify your own profile.");
        }
    }

    // ==================== ENDPOINTS ====================

    @PostMapping
    @Operation(summary = "Create instructor profile",
            description = "Only INSTRUCTOR or ADMIN role can create instructor profiles.")
    public ResponseEntity<InstructorResponse> createInstructor(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody InstructorRequest request) {
        requireRole(userRole, "INSTRUCTOR", "ADMIN");
        InstructorResponse response = instructorService.createInstructor(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{instructorId}")
    @Operation(summary = "Get instructor by ID",
            description = "Any authenticated user can view instructor profiles.")
    public ResponseEntity<InstructorResponse> getInstructorById(@PathVariable Long instructorId) {
        InstructorResponse response = instructorService.getInstructorById(instructorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all instructors",
            description = "Any authenticated user can view instructor list.")
    public ResponseEntity<List<InstructorResponse>> getAllInstructors() {
        List<InstructorResponse> responses = instructorService.getAllInstructors();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{instructorId}")
    @Operation(summary = "Update instructor profile",
            description = "Instructors can only update their own profile. Admins can update any.")
    public ResponseEntity<InstructorResponse> updateInstructor(
            @PathVariable Long instructorId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody InstructorRequest request) {
        requireRole(userRole, "INSTRUCTOR", "ADMIN");
        requireOwnershipOrAdmin(userId, instructorId, userRole);
        InstructorResponse response = instructorService.updateInstructor(instructorId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{instructorId}")
    @Operation(summary = "Delete instructor",
            description = "Only ADMIN can delete instructor accounts.")
    public ResponseEntity<String> deleteInstructor(
            @PathVariable Long instructorId,
            @RequestHeader("X-User-Role") String userRole) {
        requireRole(userRole, "ADMIN");
        String message = instructorService.deleteInstructor(instructorId);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/{instructorId}/exists")
    @Operation(summary = "Check if instructor exists",
            description = "Inter-service endpoint. Accessible by any authenticated user.")
    public ResponseEntity<Map<String, Boolean>> checkInstructorExists(@PathVariable Long instructorId) {
        boolean exists = instructorService.instructorExists(instructorId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    // ==================== REVIEW ENDPOINTS ====================

    @PostMapping("/{instructorId}/reviews")
    @Operation(summary = "Add a review for an instructor",
            description = "Only STUDENT role can submit reviews for instructors.")
    public ResponseEntity<ReviewResponse> addReview(
            @PathVariable Long instructorId,
            @RequestHeader("X-User-Id") Long studentId,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody ReviewRequest request) {
        requireRole(userRole, "STUDENT");
        ReviewResponse response = instructorService.addReview(instructorId, studentId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{instructorId}/reviews")
    @Operation(summary = "Get all reviews for an instructor")
    public ResponseEntity<List<ReviewResponse>> getReviewsByInstructor(@PathVariable Long instructorId) {
        List<ReviewResponse> responses = instructorService.getReviewsByInstructor(instructorId);
        return ResponseEntity.ok(responses);
    }
}
