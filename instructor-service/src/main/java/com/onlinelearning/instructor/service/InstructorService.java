package com.onlinelearning.instructor.service;

import com.onlinelearning.instructor.dto.*;
import com.onlinelearning.instructor.entity.*;
import com.onlinelearning.instructor.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstructorService {

    private final InstructorRepository instructorRepository;
    private final InstructorReviewRepository reviewRepository;

    // ==================== CREATE INSTRUCTOR PROFILE ====================

    public InstructorResponse createInstructor(Long instructorId, InstructorRequest request) {
        if (instructorRepository.existsById(instructorId)) {
            throw new RuntimeException("Instructor profile already exists for userId: " + instructorId);
        }
        if (instructorRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email '" + request.getEmail() + "' is already in use");
        }

        Instructor instructor = Instructor.builder()
                .instructorId(instructorId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .bio(request.getBio())
                .expertise(request.getExpertise())
                .profilePicture(request.getProfilePicture())
                .linkedinProfile(request.getLinkedinProfile())
                .websiteUrl(request.getWebsiteUrl())
                .totalCourses(0)
                .totalStudents(0)
                .rating(0.0)
                .isVerified(false)
                .build();

        instructor = instructorRepository.save(instructor);
        return mapToResponse(instructor);
    }

    // ==================== GET INSTRUCTOR BY ID ====================

    public InstructorResponse getInstructorById(Long instructorId) {
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found with ID: " + instructorId));
        return mapToResponse(instructor);
    }

    // ==================== GET ALL INSTRUCTORS ====================

    public List<InstructorResponse> getAllInstructors() {
        return instructorRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==================== UPDATE INSTRUCTOR PROFILE ====================

    public InstructorResponse updateInstructor(Long instructorId, InstructorRequest request) {
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found with ID: " + instructorId));

        instructor.setFirstName(request.getFirstName());
        instructor.setLastName(request.getLastName());
        instructor.setEmail(request.getEmail());
        instructor.setBio(request.getBio());
        instructor.setExpertise(request.getExpertise());
        instructor.setProfilePicture(request.getProfilePicture());
        instructor.setLinkedinProfile(request.getLinkedinProfile());
        instructor.setWebsiteUrl(request.getWebsiteUrl());

        instructor = instructorRepository.save(instructor);
        return mapToResponse(instructor);
    }

    // ==================== DELETE INSTRUCTOR ====================

    public String deleteInstructor(Long instructorId) {
        if (!instructorRepository.existsById(instructorId)) {
            throw new RuntimeException("Instructor not found with ID: " + instructorId);
        }
        instructorRepository.deleteById(instructorId);
        return "Instructor with ID " + instructorId + " has been deleted";
    }

    // ==================== CHECK IF INSTRUCTOR EXISTS ====================

    public boolean instructorExists(Long instructorId) {
        return instructorRepository.existsById(instructorId);
    }

    // ==================== REVIEW MANAGEMENT ====================

    public ReviewResponse addReview(Long instructorId, Long studentId, ReviewRequest request) {
        if (!instructorRepository.existsById(instructorId)) {
            throw new RuntimeException("Instructor not found with ID: " + instructorId);
        }
        if (reviewRepository.existsByInstructorIdAndStudentId(instructorId, studentId)) {
            throw new RuntimeException("You have already reviewed this instructor");
        }

        InstructorReview review = InstructorReview.builder()
                .instructorId(instructorId)
                .studentId(studentId)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        review = reviewRepository.save(review);

        // Update instructor's average rating
        updateInstructorRating(instructorId);

        return mapToReviewResponse(review);
    }

    public List<ReviewResponse> getReviewsByInstructor(Long instructorId) {
        return reviewRepository.findByInstructorId(instructorId)
                .stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    private void updateInstructorRating(Long instructorId) {
        Double avgRating = reviewRepository.getAverageRatingByInstructorId(instructorId);
        Instructor instructor = instructorRepository.findById(instructorId).orElse(null);
        if (instructor != null && avgRating != null) {
            instructor.setRating(Math.round(avgRating * 10.0) / 10.0); // Round to 1 decimal
            instructorRepository.save(instructor);
        }
    }

    private InstructorResponse mapToResponse(Instructor instructor) {
        return InstructorResponse.builder()
                .instructorId(instructor.getInstructorId())
                .firstName(instructor.getFirstName())
                .lastName(instructor.getLastName())
                .email(instructor.getEmail())
                .bio(instructor.getBio())
                .expertise(instructor.getExpertise())
                .profilePicture(instructor.getProfilePicture())
                .linkedinProfile(instructor.getLinkedinProfile())
                .websiteUrl(instructor.getWebsiteUrl())
                .totalCourses(instructor.getTotalCourses())
                .totalStudents(instructor.getTotalStudents())
                .rating(instructor.getRating())
                .isVerified(instructor.getIsVerified())
                .joinedAt(instructor.getJoinedAt())
                .build();
    }

    private ReviewResponse mapToReviewResponse(InstructorReview review) {
        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .instructorId(review.getInstructorId())
                .studentId(review.getStudentId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
