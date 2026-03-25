package com.onlinelearning.auth.repository;

import com.onlinelearning.auth.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByStudentId(Long studentId);
    List<QuizAttempt> findByQuizQuizIdAndStudentId(Long quizId, Long studentId);
    List<QuizAttempt> findByStudentIdAndIsPassedTrue(Long studentId);
}
