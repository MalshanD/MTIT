package com.onlinelearning.auth.service.impl;

import com.onlinelearning.auth.dto.QuestionDto;
import com.onlinelearning.auth.dto.QuizAttemptDto;
import com.onlinelearning.auth.dto.QuizDto;
import com.onlinelearning.auth.entity.Question;
import com.onlinelearning.auth.entity.Quiz;
import com.onlinelearning.auth.entity.QuizAttempt;
import com.onlinelearning.auth.exception.QuizException;
import com.onlinelearning.auth.repository.QuestionRepository;
import com.onlinelearning.auth.repository.QuizAttemptRepository;
import com.onlinelearning.auth.repository.QuizRepository;
import com.onlinelearning.auth.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    @Override
    @Transactional
    public QuizDto createQuiz(QuizDto quizDto) {
        Quiz quiz = new Quiz();
        BeanUtils.copyProperties(quizDto, quiz);
        Quiz savedQuiz = quizRepository.save(quiz);
        QuizDto savedDto = new QuizDto();
        BeanUtils.copyProperties(savedQuiz, savedDto);
        return savedDto;
    }

    @Override
    public QuizDto getQuizById(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException("Quiz not found with id: " + quizId));
        QuizDto quizDto = new QuizDto();
        BeanUtils.copyProperties(quiz, quizDto);
        return quizDto;
    }

    @Override
    public List<QuizDto> getAllQuizzes() {
        return quizRepository.findAll().stream()
                .map(quiz -> {
                    QuizDto dto = new QuizDto();
                    BeanUtils.copyProperties(quiz, dto);
                    return dto;
                }).collect(Collectors.toList());
    }

    @Override
    public List<QuizDto> getQuizzesByCourseId(Long courseId) {
        return quizRepository.findByCourseId(courseId).stream()
                .map(quiz -> {
                    QuizDto dto = new QuizDto();
                    BeanUtils.copyProperties(quiz, dto);
                    return dto;
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QuizDto updateQuiz(Long quizId, QuizDto quizDto) {
        Quiz existingQuiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException("Quiz not found with id: " + quizId));
        
        existingQuiz.setTitle(quizDto.getTitle());
        existingQuiz.setDescription(quizDto.getDescription());
        existingQuiz.setTotalMarks(quizDto.getTotalMarks());
        existingQuiz.setPassingMarks(quizDto.getPassingMarks());
        existingQuiz.setTimeLimit(quizDto.getTimeLimit());
        existingQuiz.setIsActive(quizDto.getIsActive());
        
        Quiz updatedQuiz = quizRepository.save(existingQuiz);
        QuizDto updatedDto = new QuizDto();
        BeanUtils.copyProperties(updatedQuiz, updatedDto);
        return updatedDto;
    }

    @Override
    @Transactional
    public void deleteQuiz(Long quizId) {
        if (!quizRepository.existsById(quizId)) {
            throw new QuizException("Quiz not found with id: " + quizId);
        }
        quizRepository.deleteById(quizId);
    }

    @Override
    @Transactional
    public QuestionDto addQuestion(Long quizId, QuestionDto questionDto) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new QuizException("Quiz not found with id: " + quizId));
        
        Question question = new Question();
        BeanUtils.copyProperties(questionDto, question);
        question.setQuiz(quiz);
        
        Question savedQuestion = questionRepository.save(question);
        QuestionDto savedDto = new QuestionDto();
        BeanUtils.copyProperties(savedQuestion, savedDto);
        savedDto.setQuizId(quiz.getQuizId());
        return savedDto;
    }

    @Override
    public List<QuestionDto> getQuestionsByQuizId(Long quizId) {
        return questionRepository.findByQuizQuizId(quizId).stream()
                .map(question -> {
                    QuestionDto dto = new QuestionDto();
                    BeanUtils.copyProperties(question, dto);
                    dto.setQuizId(question.getQuiz().getQuizId());
                    return dto;
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteQuestion(Long questionId) {
        if (!questionRepository.existsById(questionId)) {
            throw new QuizException("Question not found with id: " + questionId);
        }
        questionRepository.deleteById(questionId);
    }

    @Override
    @Transactional
    public QuizAttemptDto submitAttempt(QuizAttemptDto attemptDto) {
        Quiz quiz = quizRepository.findById(attemptDto.getQuizId())
                .orElseThrow(() -> new QuizException("Quiz not found with id: " + attemptDto.getQuizId()));

        QuizAttempt attempt = new QuizAttempt();
        BeanUtils.copyProperties(attemptDto, attempt);
        attempt.setQuiz(quiz);
        attempt.setSubmittedAt(LocalDateTime.now());
        
        // Calculate score logic could go here, or be passed in
        // For simplicity, assuming score is calculated on client or passed in DTO
        // Determine pass/fail
        if (attempt.getScore() != null && quiz.getPassingMarks() != null) {
            attempt.setIsPassed(attempt.getScore() >= quiz.getPassingMarks());
        } else {
            attempt.setIsPassed(false);
        }

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        // Check if student passed all quizzes for the course
        checkCourseCompletion(quiz.getCourseId(), attempt.getStudentId());

        QuizAttemptDto savedDto = new QuizAttemptDto();
        BeanUtils.copyProperties(savedAttempt, savedDto);
        savedDto.setQuizId(quiz.getQuizId());
        return savedDto;
    }

    private void checkCourseCompletion(Long courseId, Long studentId) {
        List<Quiz> courseQuizzes = quizRepository.findByCourseId(courseId);
        boolean allPassed = true;
        
        for (Quiz q : courseQuizzes) {
            List<QuizAttempt> attempts = quizAttemptRepository.findByQuizQuizIdAndStudentId(q.getQuizId(), studentId);
            boolean passedThisQuiz = attempts.stream().anyMatch(a -> Boolean.TRUE.equals(a.getIsPassed()));
            if (!passedThisQuiz) {
                allPassed = false;
                break;
            }
        }

        if (allPassed) {
            // Call Enrollment Service to mark as COMPLETED
            // This requires RestTemplate or WebClient usage
            // For now, leaving a placeholder
            System.out.println("Student " + studentId + " has passed all quizzes for course " + courseId);
        }
    }
}
