package com.onlinelearning.instructor.repository;

import com.onlinelearning.instructor.entity.InstructorReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstructorReviewRepository extends JpaRepository<InstructorReview, Long> {

    List<InstructorReview> findByInstructorId(Long instructorId);

    @Query("SELECT AVG(r.rating) FROM InstructorReview r WHERE r.instructorId = :instructorId")
    Double getAverageRatingByInstructorId(Long instructorId);

    boolean existsByInstructorIdAndStudentId(Long instructorId, Long studentId);
}
