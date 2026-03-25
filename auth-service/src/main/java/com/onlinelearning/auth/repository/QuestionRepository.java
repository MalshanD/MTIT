package com.onlinelearning.auth.repository;

import com.onlinelearning.auth.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuizQuizId(Long quizId);
}
