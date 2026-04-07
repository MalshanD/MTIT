package com.onlinelearning.quiz.repository;

import com.onlinelearning.quiz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuizIdOrderByOrderIndexAsc(Long quizId);
    int countByQuizId(Long quizId);

    @Transactional
    void deleteByQuizId(Long quizId);
}
