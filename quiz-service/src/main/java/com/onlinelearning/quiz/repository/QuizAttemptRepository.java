package com.onlinelearning.quiz.repository;

import com.onlinelearning.quiz.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByQuizIdAndStudentId(Long quizId, Long studentId);
    int countByQuizIdAndStudentId(Long quizId, Long studentId);
    List<QuizAttempt> findByStudentId(Long studentId);

    @Transactional
    void deleteByQuizId(Long quizId);
}
