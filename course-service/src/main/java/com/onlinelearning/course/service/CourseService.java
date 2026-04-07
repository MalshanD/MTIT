package com.onlinelearning.course.service;

import com.onlinelearning.course.dto.*;
import com.onlinelearning.course.entity.*;
import com.onlinelearning.course.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final CourseCategoryRepository categoryRepository;

    // ==================== CREATE COURSE ====================

    public CourseResponse createCourse(Long instructorId, CourseRequest request) {

        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .instructorId(instructorId)
                .thumbnailUrl(request.getThumbnailUrl())
                .price(request.getPrice())
                .level(CourseLevel.valueOf(request.getLevel()))
                .language(request.getLanguage() != null ? request.getLanguage() : "English")
                .isPublished(false)
                .totalLessons(0)
                .build();

        // Set category if provided
        if (request.getCategoryId() != null) {
            CourseCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));
            course.setCategory(category);
        }

        course = courseRepository.save(course);
        return mapToResponse(course);
    }

    // ==================== GET COURSE BY ID ====================

    public CourseResponse getCourseById(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));
        return mapToResponse(course);
    }

    // ==================== GET ALL PUBLISHED COURSES ====================

    public List<CourseResponse> getAllPublishedCourses() {
        return courseRepository.findByIsPublishedTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==================== GET ALL COURSES ====================

    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==================== GET COURSES BY INSTRUCTOR ====================

    public List<CourseResponse> getCoursesByInstructor(Long instructorId) {
        return courseRepository.findByInstructorId(instructorId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==================== GET COURSES BY CATEGORY ====================

    public List<CourseResponse> getCoursesByCategory(Long categoryId) {
        return courseRepository.findByCategoryCategoryId(categoryId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==================== SEARCH COURSES ====================

    public List<CourseResponse> searchCourses(String keyword) {
        return courseRepository.findByTitleContainingIgnoreCase(keyword)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==================== UPDATE COURSE ====================

    public CourseResponse updateCourse(Long courseId, Long instructorId, CourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));

        // Verify the instructor owns this course
        if (!course.getInstructorId().equals(instructorId)) {
            throw new RuntimeException("You are not authorized to update this course");
        }

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setThumbnailUrl(request.getThumbnailUrl());
        course.setPrice(request.getPrice());
        course.setLevel(CourseLevel.valueOf(request.getLevel()));
        course.setLanguage(request.getLanguage() != null ? request.getLanguage() : course.getLanguage());

        if (request.getCategoryId() != null) {
            CourseCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));
            course.setCategory(category);
        }

        course = courseRepository.save(course);
        return mapToResponse(course);
    }

    // ==================== PUBLISH / UNPUBLISH COURSE ====================

    public CourseResponse publishCourse(Long courseId, Long instructorId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));

        if (!course.getInstructorId().equals(instructorId)) {
            throw new RuntimeException("You are not authorized to publish this course");
        }

        course.setIsPublished(true);
        course = courseRepository.save(course);
        return mapToResponse(course);
    }

    // ==================== DELETE COURSE ====================

    public String deleteCourse(Long courseId, Long instructorId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));

        if (!course.getInstructorId().equals(instructorId)) {
            throw new RuntimeException("You are not authorized to delete this course");
        }

        courseRepository.delete(course);
        return "Course with ID " + courseId + " has been deleted";
    }

    // ==================== CHECK IF COURSE EXISTS ====================
    // Called by Enrollment Service, Quiz Service, Certificate Service

    public boolean courseExists(Long courseId) {
        return courseRepository.existsById(courseId);
    }

    // ==================== LESSON MANAGEMENT ====================

    public LessonResponse addLesson(Long courseId, LessonRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));

        Lesson lesson = Lesson.builder()
                .courseId(courseId)
                .title(request.getTitle())
                .content(request.getContent())
                .videoUrl(request.getVideoUrl())
                .duration(request.getDuration())
                .orderIndex(request.getOrderIndex())
                .isPreview(request.getIsPreview() != null ? request.getIsPreview() : false)
                .build();

        lesson = lessonRepository.save(lesson);

        // Update total lessons count on the course
        int totalLessons = lessonRepository.countByCourseId(courseId);
        course.setTotalLessons(totalLessons);
        courseRepository.save(course);

        return mapToLessonResponse(lesson);
    }

    public List<LessonResponse> getLessonsByCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new RuntimeException("Course not found with ID: " + courseId);
        }
        return lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId)
                .stream()
                .map(this::mapToLessonResponse)
                .collect(Collectors.toList());
    }

    public LessonResponse updateLesson(Long lessonId, LessonRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found with ID: " + lessonId));

        lesson.setTitle(request.getTitle());
        lesson.setContent(request.getContent());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setDuration(request.getDuration());
        lesson.setOrderIndex(request.getOrderIndex());
        lesson.setIsPreview(request.getIsPreview() != null ? request.getIsPreview() : lesson.getIsPreview());

        lesson = lessonRepository.save(lesson);
        return mapToLessonResponse(lesson);
    }

    public String deleteLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found with ID: " + lessonId));

        Long courseId = lesson.getCourseId();
        lessonRepository.delete(lesson);

        // Update total lessons count
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            int totalLessons = lessonRepository.countByCourseId(courseId);
            course.setTotalLessons(totalLessons);
            courseRepository.save(course);
        }

        return "Lesson with ID " + lessonId + " has been deleted";
    }

    // ==================== MAPPER METHODS ====================

    private CourseResponse mapToResponse(Course course) {
        return CourseResponse.builder()
                .courseId(course.getCourseId())
                .title(course.getTitle())
                .description(course.getDescription())
                .instructorId(course.getInstructorId())
                .categoryId(course.getCategory() != null ? course.getCategory().getCategoryId() : null)
                .categoryName(course.getCategory() != null ? course.getCategory().getName() : null)
                .thumbnailUrl(course.getThumbnailUrl())
                .price(course.getPrice())
                .level(course.getLevel().name())
                .language(course.getLanguage())
                .isPublished(course.getIsPublished())
                .totalLessons(course.getTotalLessons())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    private LessonResponse mapToLessonResponse(Lesson lesson) {
        return LessonResponse.builder()
                .lessonId(lesson.getLessonId())
                .courseId(lesson.getCourseId())
                .title(lesson.getTitle())
                .content(lesson.getContent())
                .videoUrl(lesson.getVideoUrl())
                .duration(lesson.getDuration())
                .orderIndex(lesson.getOrderIndex())
                .isPreview(lesson.getIsPreview())
                .build();
    }
}
