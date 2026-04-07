package com.onlinelearning.quiz.controller;

import com.onlinelearning.quiz.dto.*;
import com.onlinelearning.quiz.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quiz Service", description = "Manage quizzes, questions, and student attempts")
public class QuizController {

    private final QuizService quizService;

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
                    "Access denied. You can only access your own quiz attempts.");
        }
    }

    // ==================== QUIZ ENDPOINTS ====================

    @PostMapping
    @Operation(summary = "Create a new quiz",
            description = "Only INSTRUCTOR or ADMIN can create quizzes.")
    public ResponseEntity<QuizResponse> createQuiz(
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody QuizRequest request) {
        requireRole(userRole, "INSTRUCTOR", "ADMIN");
        return new ResponseEntity<>(quizService.createQuiz(request), HttpStatus.CREATED);
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get all active quizzes for a course")
    public ResponseEntity<List<QuizResponse>> getQuizzesByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(quizService.getQuizzesByCourse(courseId));
    }

    @GetMapping("/{quizId}")
    @Operation(summary = "Get quiz by ID")
    public ResponseEntity<QuizResponse> getQuizById(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuizById(quizId));
    }

    @DeleteMapping("/{quizId}")
    @Operation(summary = "Delete a quiz and all its questions and attempts",
            description = "Only INSTRUCTOR or ADMIN can delete quizzes.")
    public ResponseEntity<Void> deleteQuiz(
            @PathVariable Long quizId,
            @RequestHeader("X-User-Role") String userRole) {
        requireRole(userRole, "INSTRUCTOR", "ADMIN");
        quizService.deleteQuiz(quizId);
        return ResponseEntity.noContent().build();
    }

    // ==================== QUESTION ENDPOINTS ====================

    @PostMapping("/{quizId}/questions")
    @Operation(summary = "Add a question to a quiz",
            description = "Only INSTRUCTOR or ADMIN can add questions.")
    public ResponseEntity<QuestionResponse> addQuestion(
            @PathVariable Long quizId,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody QuestionRequest request) {
        requireRole(userRole, "INSTRUCTOR", "ADMIN");
        return new ResponseEntity<>(quizService.addQuestion(quizId, request), HttpStatus.CREATED);
    }

    @GetMapping("/{quizId}/questions")
    @Operation(summary = "Get all questions for a quiz")
    public ResponseEntity<List<QuestionResponse>> getQuestions(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuestionsByQuiz(quizId));
    }

    @DeleteMapping("/{quizId}/questions/{questionId}")
    @Operation(summary = "Delete a question from a quiz",
            description = "Only INSTRUCTOR or ADMIN can delete questions.")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long quizId,
            @PathVariable Long questionId,
            @RequestHeader("X-User-Role") String userRole) {
        requireRole(userRole, "INSTRUCTOR", "ADMIN");
        quizService.deleteQuestion(quizId, questionId);
        return ResponseEntity.noContent().build();
    }

    // ==================== ATTEMPT / SUBMIT ENDPOINTS ====================

    @PostMapping("/submit")
    @Operation(summary = "Submit quiz answers",
            description = "Only STUDENT can submit quizzes. Auto-grades and triggers completion if passed.")
    public ResponseEntity<AttemptResponse> submitQuiz(
            @RequestHeader("X-User-Id") Long studentId,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody SubmitAnswerRequest request) {
        requireRole(userRole, "STUDENT");
        return new ResponseEntity<>(quizService.submitQuiz(studentId, request), HttpStatus.CREATED);
    }

    @GetMapping("/attempts/student/{studentId}")
    @Operation(summary = "Get all quiz attempts by a student",
            description = "Students can view own attempts. Instructors and Admins can view any.")
    public ResponseEntity<List<AttemptResponse>> getAttemptsByStudent(
            @PathVariable Long studentId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {
        if ("STUDENT".equalsIgnoreCase(userRole)) {
            requireOwnershipOrAdmin(userId, studentId, userRole);
        }
        return ResponseEntity.ok(quizService.getAttemptsByStudent(studentId));
    }

    @GetMapping("/{quizId}/attempts/{studentId}")
    @Operation(summary = "Get attempts for a specific quiz by a student",
            description = "Students can view own attempts. Instructors and Admins can view any.")
    public ResponseEntity<List<AttemptResponse>> getAttempts(
            @PathVariable Long quizId,
            @PathVariable Long studentId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {
        if ("STUDENT".equalsIgnoreCase(userRole)) {
            requireOwnershipOrAdmin(userId, studentId, userRole);
        }
        return ResponseEntity.ok(quizService.getAttemptsByQuizAndStudent(quizId, studentId));
    }
}
