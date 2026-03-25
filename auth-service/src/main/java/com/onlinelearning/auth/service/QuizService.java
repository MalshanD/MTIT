package com.onlinelearning.auth.service;

import com.onlinelearning.auth.dto.QuestionDto;
import com.onlinelearning.auth.dto.QuizAttemptDto;
import com.onlinelearning.auth.dto.QuizDto;
import java.util.List;

public interface QuizService {
    QuizDto createQuiz(QuizDto quizDto);
    QuizDto getQuizById(Long quizId);
    List<QuizDto> getAllQuizzes();
    List<QuizDto> getQuizzesByCourseId(Long courseId);
    QuizDto updateQuiz(Long quizId, QuizDto quizDto);
    void deleteQuiz(Long quizId);

    QuestionDto addQuestion(Long quizId, QuestionDto questionDto);
    List<QuestionDto> getQuestionsByQuizId(Long quizId);
    void deleteQuestion(Long questionId);

    QuizAttemptDto submitAttempt(QuizAttemptDto attemptDto);
}
