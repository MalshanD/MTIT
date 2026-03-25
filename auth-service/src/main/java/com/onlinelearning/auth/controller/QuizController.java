package com.onlinelearning.auth.controller;

import com.onlinelearning.auth.dto.QuestionDto;
import com.onlinelearning.auth.dto.QuizAttemptDto;
import com.onlinelearning.auth.dto.QuizDto;
import com.onlinelearning.auth.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // Quiz Management

    @PostMapping
    public ResponseEntity<QuizDto> createQuiz(@RequestBody QuizDto quizDto) {
        return ResponseEntity.ok(quizService.createQuiz(quizDto));
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<QuizDto> getQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuizById(quizId));
    }

    @GetMapping
    public ResponseEntity<List<QuizDto>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<QuizDto>> getQuizzesByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(quizService.getQuizzesByCourseId(courseId));
    }

    @PutMapping("/{quizId}")
    public ResponseEntity<QuizDto> updateQuiz(@PathVariable Long quizId, @RequestBody QuizDto quizDto) {
        return ResponseEntity.ok(quizService.updateQuiz(quizId, quizDto));
    }

    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long quizId) {
        quizService.deleteQuiz(quizId);
        return ResponseEntity.noContent().build();
    }

    // Question Management

    @PostMapping("/{quizId}/questions")
    public ResponseEntity<QuestionDto> addQuestion(@PathVariable Long quizId, @RequestBody QuestionDto questionDto) {
        return ResponseEntity.ok(quizService.addQuestion(quizId, questionDto));
    }

    @GetMapping("/{quizId}/questions")
    public ResponseEntity<List<QuestionDto>> getQuestionsByQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuestionsByQuizId(quizId));
    }

    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        quizService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    // Student Interaction

    @PostMapping("/submit")
    public ResponseEntity<QuizAttemptDto> submitQuiz(@RequestBody QuizAttemptDto attemptDto) {
        return ResponseEntity.ok(quizService.submitAttempt(attemptDto));
    }
}
