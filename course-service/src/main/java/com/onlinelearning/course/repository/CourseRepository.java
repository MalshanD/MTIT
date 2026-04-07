package com.onlinelearning.course.repository;

import com.onlinelearning.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByInstructorId(Long instructorId);

    List<Course> findByIsPublishedTrue();

    List<Course> findByCategoryCategoryId(Long categoryId);

    List<Course> findByTitleContainingIgnoreCase(String keyword);
}
