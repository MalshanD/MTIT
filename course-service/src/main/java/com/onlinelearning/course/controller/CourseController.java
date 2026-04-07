package com.onlinelearning.course.controller;

import com.onlinelearning.course.dto.*;
import com.onlinelearning.course.service.CourseService;
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
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Course Service", description = "Manage courses, lessons, and content")
public class CourseController {

    private final CourseService courseService;

    // ==================== ROLE HELPERS ====================

    private void requireRole(String role, String... allowed) {
        for (String r : allowed) {
            if (r.equalsIgnoreCase(role)) return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Access denied. Required role: " + String.join(" or ", allowed));
    }

    // ==================== COURSE ENDPOINTS ====================

    @PostMapping
    @Operation(summary = "Create a new course",
            description = "Only INSTRUCTOR or ADMIN can create courses.")
    public ResponseEntity<CourseResponse> createCourse(
            @RequestHeader("X-User-Id") Long instructorId,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody CourseRequest request) {
        requireRole(userRole, "INSTRUCTOR", "ADMIN");
        CourseResponse response = courseService.createCourse(instructorId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{courseId}")
    @Operation(summary = "Get course by ID")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Long courseId) {
        CourseResponse response = courseService.getCourseById(courseId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all published courses", description = "Returns only published courses visible to students")
    public ResponseEntity<List<CourseResponse>> getAllPublishedCourses() {
        List<CourseResponse> responses = courseService.getAllPublishedCourses();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all courses",
            description = "Returns all courses including unpublished. Only INSTRUCTOR or ADMIN.")
    public ResponseEntity<List<CourseResponse>> getAllCourses(
            @RequestHeader("X-User-Role") String userRole) {
        requireRole(userRole, "INSTRUCTOR", "ADMIN");
        List<CourseResponse> responses = courseService.getAllCourses();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/instructor/{instructorId}")
    @Operation(summary = "Get courses by instructor")
    public ResponseEntity<List<CourseResponse>> getCoursesByInstructor(@PathVariable Long instructorId) {
        List<CourseResponse> responses = courseService.getCoursesByInstructor(instructorId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get courses by category")
    public ResponseEntity<List<CourseResponse>> getCoursesByCategory(@PathVariable Long categoryId) {
        List<CourseResponse> responses = courseService.getCoursesByCategory(categoryId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    @Operation(summary = "Search courses by keyword")
    public ResponseEntity<List<CourseResponse>> searchCourses(@RequestParam String keyword) {
        List<CourseResponse> responses = courseService.searchCourses(keyword);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{courseId}")
    @Operation(summary = "Update a course",
            description = "Only the course owner (INSTRUCTOR) or ADMIN can update.")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long courseId,
            @RequestHeader("X-User-Id") Long instructorId,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody CourseRequest request) {
        requireRole(userRole, "INSTRUCTOR", "ADMIN");
        CourseResponse response = courseService.updateCourse(courseId, instructorId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{courseId}/publish")
    @Operation(summary = "Publish a course",
            description = "Only the course owner (INSTRUCTOR) or ADMIN can publish.")
    public ResponseEntity<CourseResponse> publishCourse(
            @PathVariable Long courseId,
            @RequestHeader("X-User-Id") Long instructorId,
            @RequestHeader("X-User-Role") String userRole) {
        requireRole(userRole, "INSTRUCTOR", "ADMIN");
        CourseResponse response = courseService.publishCourse(courseId, instructorId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{courseId}")
    @Operation(summary = "Delete a course",
            description = "Only the course owner (INSTRUCTOR) or ADMIN can delete.")
    public ResponseEntity<String> deleteCourse(
            @PathVariable Long courseId,
            @RequestHeader("X-User-Id") Long instructorId,
            @RequestHeader("X-User-Role") String userRole) {
        requireRole(userRole, "INSTRUCTOR", "ADMIN");
        String message = courseService.deleteCourse(courseId, instructorId);
        return ResponseEntity.ok(message);
    }

    // ==================== EXISTS CHECK (Inter-service) ====================

    @GetMapping("/{courseId}/exists")
    @Operation(summary = "Check if course exists",
            description = "Used by Enrollment, Quiz, and Certificate services to verify course")
    public ResponseEntity<Map<String, Boolean>> checkCourseExists(@PathVariable Long courseId) {
        boolean exists = courseService.courseExists(courseId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    // ==================== LESSON ENDPOINTS ====================

    @PostMapping("/{courseId}/lessons")
    @Operation(summary = "Add a lesson to a course",
            description = "Only INSTRUCTOR or ADMIN can add lessons.")
    public ResponseEntity<LessonResponse> addLesson(
            @PathVariable Long courseId,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody LessonRequest request) {
        requireRole(userRole, "INSTRUCTOR", "ADMIN");
        LessonResponse response = courseService.addLesson(courseId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{courseId}/lessons")
    @Operation(summary = "Get all lessons for a course", description = "Returns lessons ordered by orderIndex")
    public ResponseEntity<List<LessonResponse>> getLessonsByCourse(@PathVariable Long courseId) {
        List<LessonResponse> responses = courseService.getLessonsByCourse(courseId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/lessons/{lessonId}")
    @Operation(summary = "Update a lesson",
            description = "Only INSTRUCTOR or ADMIN can update lessons.")
    public ResponseEntity<LessonResponse> updateLesson(
            @PathVariable Long lessonId,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody LessonRequest request) {
        requireRole(userRole, "INSTRUCTOR", "ADMIN");
        LessonResponse response = courseService.updateLesson(lessonId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/lessons/{lessonId}")
    @Operation(summary = "Delete a lesson",
            description = "Only INSTRUCTOR or ADMIN can delete lessons.")
    public ResponseEntity<String> deleteLesson(
            @PathVariable Long lessonId,
            @RequestHeader("X-User-Role") String userRole) {
        requireRole(userRole, "INSTRUCTOR", "ADMIN");
        String message = courseService.deleteLesson(lessonId);
        return ResponseEntity.ok(message);
    }
}
