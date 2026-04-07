package com.onlinelearning.student.service;

import com.onlinelearning.student.dto.*;
import com.onlinelearning.student.entity.*;
import com.onlinelearning.student.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentProgressRepository progressRepository;

    // ==================== CREATE STUDENT PROFILE ====================

    public StudentResponse createStudent(Long studentId, StudentRequest request) {

        // Check if profile already exists for this userId
        if (studentRepository.existsById(studentId)) {
            throw new RuntimeException("Student profile already exists for userId: " + studentId);
        }

        // Check if email is already used
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email '" + request.getEmail() + "' is already in use");
        }

        Student student = Student.builder()
                .studentId(studentId) // Same as userId from Auth Service
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .dateOfBirth(request.getDateOfBirth())
                .phoneNumber(request.getPhoneNumber())
                .profilePicture(request.getProfilePicture())
                .bio(request.getBio())
                .isActive(true)
                .build();

        student = studentRepository.save(student);
        return mapToResponse(student);
    }

    // ==================== GET STUDENT BY ID ====================

    public StudentResponse getStudentById(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));
        return mapToResponse(student);
    }

    // ==================== GET ALL STUDENTS ====================

    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==================== UPDATE STUDENT PROFILE ====================

    public StudentResponse updateStudent(Long studentId, StudentRequest request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setEmail(request.getEmail());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setPhoneNumber(request.getPhoneNumber());
        student.setProfilePicture(request.getProfilePicture());
        student.setBio(request.getBio());

        student = studentRepository.save(student);
        return mapToResponse(student);
    }

    // ==================== DELETE STUDENT ====================

    public String deleteStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        // Soft delete - deactivate instead of removing
        student.setIsActive(false);
        studentRepository.save(student);

        return "Student with ID " + studentId + " has been deactivated";
    }

    // ==================== CHECK IF STUDENT EXISTS ====================
    // Called by other services (Enrollment, Certificate) via HTTP

    public boolean studentExists(Long studentId) {
        return studentRepository.existsById(studentId);
    }

    // ==================== PROGRESS TRACKING ====================

    public ProgressResponse createOrUpdateProgress(Long studentId, ProgressRequest request) {

        // Check student exists
        if (!studentRepository.existsById(studentId)) {
            throw new RuntimeException("Student not found with ID: " + studentId);
        }

        StudentProgress progress = progressRepository
                .findByStudentIdAndCourseId(studentId, request.getCourseId())
                .orElse(StudentProgress.builder()
                        .studentId(studentId)
                        .courseId(request.getCourseId())
                        .completedLessons(0)
                        .totalLessons(0)
                        .progressPercent(0.0)
                        .isCompleted(false)
                        .build());

        progress.setCompletedLessons(request.getCompletedLessons());
        progress.setTotalLessons(request.getTotalLessons());
        progress.setLastAccessedAt(LocalDateTime.now());

        // Calculate progress percentage
        if (request.getTotalLessons() > 0) {
            double percent = ((double) request.getCompletedLessons() / request.getTotalLessons()) * 100;
            progress.setProgressPercent(percent);
            progress.setIsCompleted(percent >= 100.0);
        }

        progress = progressRepository.save(progress);
        return mapToProgressResponse(progress);
    }

    public List<ProgressResponse> getStudentProgress(Long studentId) {
        return progressRepository.findByStudentId(studentId)
                .stream()
                .map(this::mapToProgressResponse)
                .collect(Collectors.toList());
    }

    public ProgressResponse getProgressByCourse(Long studentId, Long courseId) {
        StudentProgress progress = progressRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new RuntimeException(
                        "No progress found for student " + studentId + " in course " + courseId));
        return mapToProgressResponse(progress);
    }

    // ==================== MAPPER METHODS ====================

    private StudentResponse mapToResponse(Student student) {
        return StudentResponse.builder()
                .studentId(student.getStudentId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .dateOfBirth(student.getDateOfBirth())
                .phoneNumber(student.getPhoneNumber())
                .profilePicture(student.getProfilePicture())
                .bio(student.getBio())
                .registeredAt(student.getRegisteredAt())
                .isActive(student.getIsActive())
                .build();
    }

    private ProgressResponse mapToProgressResponse(StudentProgress progress) {
        return ProgressResponse.builder()
                .progressId(progress.getProgressId())
                .studentId(progress.getStudentId())
                .courseId(progress.getCourseId())
                .completedLessons(progress.getCompletedLessons())
                .totalLessons(progress.getTotalLessons())
                .progressPercent(progress.getProgressPercent())
                .lastAccessedAt(progress.getLastAccessedAt())
                .isCompleted(progress.getIsCompleted())
                .build();
    }
}
