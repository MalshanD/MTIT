package com.onlinelearning.quiz.service;

import com.onlinelearning.quiz.dto.*;
import com.onlinelearning.quiz.entity.*;
import com.onlinelearning.quiz.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final RestTemplate restTemplate;

    @Value("${services.enrollment.url}")
    private String enrollmentServiceUrl;

    // ==================== QUIZ CRUD ====================

    public QuizResponse createQuiz(QuizRequest request) {
        Quiz quiz = Quiz.builder()
                .courseId(request.getCourseId())
                .title(request.getTitle())
                .description(request.getDescription())
                .totalMarks(request.getTotalMarks())
                .passingMarks(request.getPassingMarks())
                .timeLimit(request.getTimeLimit() != null ? request.getTimeLimit() : 0)
                .isActive(true)
                .build();
        quiz = quizRepository.save(quiz);
        return mapToQuizResponse(quiz);
    }

    public List<QuizResponse> getQuizzesByCourse(Long courseId) {
        return quizRepository.findByCourseIdAndIsActiveTrue(courseId)
                .stream().map(this::mapToQuizResponse).collect(Collectors.toList());
    }

    public QuizResponse getQuizById(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with ID: " + quizId));
        return mapToQuizResponse(quiz);
    }

    @Transactional
    public void deleteQuiz(Long quizId) {
        if (!quizRepository.existsById(quizId)) {
            throw new RuntimeException("Quiz not found with ID: " + quizId);
        }
        // Delete all related attempts first, then questions, then the quiz itself
        attemptRepository.deleteByQuizId(quizId);
        questionRepository.deleteByQuizId(quizId);
        quizRepository.deleteById(quizId);
    }

    // ==================== QUESTION CRUD ====================

    public QuestionResponse addQuestion(Long quizId, QuestionRequest request) {
        if (!quizRepository.existsById(quizId)) {
            throw new RuntimeException("Quiz not found with ID: " + quizId);
        }
        Question question = Question.builder()
                .quizId(quizId)
                .questionText(request.getQuestionText())
                .optionA(request.getOptionA())
                .optionB(request.getOptionB())
                .optionC(request.getOptionC())
                .optionD(request.getOptionD())
                .correctOption(CorrectOption.valueOf(request.getCorrectOption()))
                .marks(request.getMarks())
                .orderIndex(request.getOrderIndex())
                .build();
        question = questionRepository.save(question);
        return mapToQuestionResponse(question);
    }

    public List<QuestionResponse> getQuestionsByQuiz(Long quizId) {
        return questionRepository.findByQuizIdOrderByOrderIndexAsc(quizId)
                .stream().map(this::mapToQuestionResponse).collect(Collectors.toList());
    }

    public void deleteQuestion(Long quizId, Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with ID: " + questionId));
        if (!question.getQuizId().equals(quizId)) {
            throw new RuntimeException("Question does not belong to quiz ID: " + quizId);
        }
        questionRepository.deleteById(questionId);
    }

    // ==================== SUBMIT QUIZ (Auto-grade) ====================

    public AttemptResponse submitQuiz(Long studentId, SubmitAnswerRequest request) {
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        List<Question> questions = questionRepository.findByQuizIdOrderByOrderIndexAsc(quiz.getQuizId());

        // Calculate score
        int score = 0;
        for (Question q : questions) {
            String selectedOption = request.getAnswers().get(q.getQuestionId());
            if (selectedOption != null && selectedOption.equals(q.getCorrectOption().name())) {
                score += q.getMarks();
            }
        }

        boolean passed = score >= quiz.getPassingMarks();
        int attemptNumber = attemptRepository.countByQuizIdAndStudentId(quiz.getQuizId(), studentId) + 1;

        QuizAttempt attempt = QuizAttempt.builder()
                .quizId(quiz.getQuizId())
                .studentId(studentId)
                .score(score)
                .isPassed(passed)
                .attemptNumber(attemptNumber)
                .submittedAt(LocalDateTime.now())
                .build();

        attempt = attemptRepository.save(attempt);

        // If passed, notify Enrollment Service to mark course as completed
        if (passed) {
            try {
                String url = enrollmentServiceUrl + "/api/enrollments/complete?studentId=" + studentId + "&courseId=" + quiz.getCourseId();
                restTemplate.put(url, null);
            } catch (Exception e) {
                System.err.println("Warning: Could not notify Enrollment Service: " + e.getMessage());
            }
        }

        return mapToAttemptResponse(attempt);
    }

    public List<AttemptResponse> getAttemptsByStudent(Long studentId) {
        return attemptRepository.findByStudentId(studentId)
                .stream().map(this::mapToAttemptResponse).collect(Collectors.toList());
    }

    public List<AttemptResponse> getAttemptsByQuizAndStudent(Long quizId, Long studentId) {
        return attemptRepository.findByQuizIdAndStudentId(quizId, studentId)
                .stream().map(this::mapToAttemptResponse).collect(Collectors.toList());
    }

    // ==================== MAPPERS ====================

    private QuizResponse mapToQuizResponse(Quiz q) {
        return QuizResponse.builder().quizId(q.getQuizId()).courseId(q.getCourseId())
                .title(q.getTitle()).description(q.getDescription())
                .totalMarks(q.getTotalMarks()).passingMarks(q.getPassingMarks())
                .timeLimit(q.getTimeLimit()).isActive(q.getIsActive()).build();
    }

    private QuestionResponse mapToQuestionResponse(Question q) {
        return QuestionResponse.builder().questionId(q.getQuestionId()).quizId(q.getQuizId())
                .questionText(q.getQuestionText()).optionA(q.getOptionA()).optionB(q.getOptionB())
                .optionC(q.getOptionC()).optionD(q.getOptionD())
                .correctOption(q.getCorrectOption().name()).marks(q.getMarks())
                .orderIndex(q.getOrderIndex()).build();
    }

    private AttemptResponse mapToAttemptResponse(QuizAttempt a) {
        return AttemptResponse.builder().attemptId(a.getAttemptId()).quizId(a.getQuizId())
                .studentId(a.getStudentId()).startedAt(a.getStartedAt()).submittedAt(a.getSubmittedAt())
                .score(a.getScore()).isPassed(a.getIsPassed()).attemptNumber(a.getAttemptNumber()).build();
    }
}
