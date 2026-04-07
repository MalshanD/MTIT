package com.onlinelearning.student.controller;

import com.onlinelearning.student.dto.*;
import com.onlinelearning.student.service.StudentService;
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
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Student Service", description = "Manage student profiles and progress tracking")
public class StudentController {

    private final StudentService studentService;

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
                    "Access denied. You can only access your own profile.");
        }
    }

    // ==================== CREATE STUDENT PROFILE ====================

    @PostMapping
    @Operation(summary = "Create student profile",
            description = "Creates a new student profile. Only STUDENT or ADMIN role allowed.")
    public ResponseEntity<StudentResponse> createStudent(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody StudentRequest request) {
        requireRole(userRole, "STUDENT", "ADMIN");
        StudentResponse response = studentService.createStudent(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ==================== GET STUDENT BY ID ====================

    @GetMapping("/{studentId}")
    @Operation(summary = "Get student by ID",
            description = "Students can view their own profile. Instructors and Admins can view any.")
    public ResponseEntity<StudentResponse> getStudentById(
            @PathVariable Long studentId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {
        // Students can only view their own profile; Instructors & Admins can view any
        if ("STUDENT".equalsIgnoreCase(userRole)) {
            requireOwnershipOrAdmin(userId, studentId, userRole);
        }
        StudentResponse response = studentService.getStudentById(studentId);
        return ResponseEntity.ok(response);
    }

    // ==================== GET ALL STUDENTS ====================

    @GetMapping
    @Operation(summary = "Get all students",
            description = "Only ADMIN and INSTRUCTOR roles can list all students.")
    public ResponseEntity<List<StudentResponse>> getAllStudents(
            @RequestHeader("X-User-Role") String userRole) {
        requireRole(userRole, "ADMIN", "INSTRUCTOR");
        List<StudentResponse> responses = studentService.getAllStudents();
        return ResponseEntity.ok(responses);
    }

    // ==================== UPDATE STUDENT PROFILE ====================

    @PutMapping("/{studentId}")
    @Operation(summary = "Update student profile",
            description = "Students can update only their own profile. Admins can update any.")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable Long studentId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody StudentRequest request) {
        requireRole(userRole, "STUDENT", "ADMIN");
        requireOwnershipOrAdmin(userId, studentId, userRole);
        StudentResponse response = studentService.updateStudent(studentId, request);
        return ResponseEntity.ok(response);
    }

    // ==================== DELETE (DEACTIVATE) STUDENT ====================

    @DeleteMapping("/{studentId}")
    @Operation(summary = "Deactivate student",
            description = "Only ADMIN can deactivate student accounts.")
    public ResponseEntity<String> deleteStudent(
            @PathVariable Long studentId,
            @RequestHeader("X-User-Role") String userRole) {
        requireRole(userRole, "ADMIN");
        String message = studentService.deleteStudent(studentId);
        return ResponseEntity.ok(message);
    }

    // ==================== CHECK IF STUDENT EXISTS ====================
    // Called internally by Enrollment Service, Certificate Service

    @GetMapping("/{studentId}/exists")
    @Operation(summary = "Check if student exists",
            description = "Inter-service endpoint. Accessible by any authenticated user.")
    public ResponseEntity<Map<String, Boolean>> checkStudentExists(@PathVariable Long studentId) {
        boolean exists = studentService.studentExists(studentId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    // ==================== PROGRESS TRACKING ====================

    @PostMapping("/{studentId}/progress")
    @Operation(summary = "Create or update progress",
            description = "Students can update only their own progress. Admins can update any.")
    public ResponseEntity<ProgressResponse> createOrUpdateProgress(
            @PathVariable Long studentId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody ProgressRequest request) {
        requireRole(userRole, "STUDENT", "ADMIN");
        requireOwnershipOrAdmin(userId, studentId, userRole);
        ProgressResponse response = studentService.createOrUpdateProgress(studentId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{studentId}/progress")
    @Operation(summary = "Get all progress for a student",
            description = "Students can view own progress. Instructors and Admins can view any.")
    public ResponseEntity<List<ProgressResponse>> getStudentProgress(
            @PathVariable Long studentId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {
        if ("STUDENT".equalsIgnoreCase(userRole)) {
            requireOwnershipOrAdmin(userId, studentId, userRole);
        }
        List<ProgressResponse> responses = studentService.getStudentProgress(studentId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{studentId}/progress/{courseId}")
    @Operation(summary = "Get progress for a specific course",
            description = "Students can view own progress. Instructors and Admins can view any.")
    public ResponseEntity<ProgressResponse> getProgressByCourse(
            @PathVariable Long studentId,
            @PathVariable Long courseId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {
        if ("STUDENT".equalsIgnoreCase(userRole)) {
            requireOwnershipOrAdmin(userId, studentId, userRole);
        }
        ProgressResponse response = studentService.getProgressByCourse(studentId, courseId);
        return ResponseEntity.ok(response);
    }
}
