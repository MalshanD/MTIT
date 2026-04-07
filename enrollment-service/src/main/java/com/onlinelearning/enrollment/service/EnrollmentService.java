package com.onlinelearning.enrollment.service;

import com.onlinelearning.enrollment.dto.*;
import com.onlinelearning.enrollment.entity.*;
import com.onlinelearning.enrollment.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final WishListRepository wishListRepository;
    private final RestTemplate restTemplate;

    @Value("${services.student.url}")
    private String studentServiceUrl;

    @Value("${services.course.url}")
    private String courseServiceUrl;

    @Value("${services.certificate.url}")
    private String certificateServiceUrl;

    // ==================== ENROLL STUDENT ====================

    public EnrollmentResponse enrollStudent(Long studentId, EnrollRequest request) {

        // Check if already enrolled
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, request.getCourseId())) {
            throw new RuntimeException("Student is already enrolled in this course");
        }

        // Verify student exists (call Student Service)
        verifyStudentExists(studentId);

        // Verify course exists (call Course Service)
        verifyCourseExists(request.getCourseId());

        Enrollment enrollment = Enrollment.builder()
                .studentId(studentId)
                .courseId(request.getCourseId())
                .status(EnrollmentStatus.ACTIVE)
                .paymentId(request.getPaymentId())
                .amountPaid(request.getAmountPaid() != null ? request.getAmountPaid() : 0.0)
                .build();

        enrollment = enrollmentRepository.save(enrollment);

        // Remove from wishlist if it was there
        wishListRepository.findByStudentIdAndCourseId(studentId, request.getCourseId())
                .ifPresent(wishListRepository::delete);

        return mapToResponse(enrollment);
    }

    // ==================== GET ENROLLMENTS BY STUDENT ====================

    public List<EnrollmentResponse> getEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==================== GET ENROLLMENTS BY COURSE ====================

    public List<EnrollmentResponse> getEnrollmentsByCourse(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==================== MARK ENROLLMENT AS COMPLETED ====================
    // Called by Quiz Service when all quizzes are passed

    public EnrollmentResponse markAsCompleted(Long studentId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found for student " + studentId + " in course " + courseId));

        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        enrollment.setCompletedAt(LocalDateTime.now());
        enrollment = enrollmentRepository.save(enrollment);

        // Trigger certificate issuance (call Certificate Service)
        try {
            String certUrl = certificateServiceUrl + "/api/certificates/issue?studentId=" + studentId + "&courseId=" + courseId;
            restTemplate.postForEntity(certUrl, null, String.class);
        } catch (Exception e) {
            // Log but don't fail the enrollment completion
            System.err.println("Warning: Could not trigger certificate issuance: " + e.getMessage());
        }

        return mapToResponse(enrollment);
    }

    // ==================== DROP ENROLLMENT ====================

    public EnrollmentResponse dropEnrollment(Long studentId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollment = enrollmentRepository.save(enrollment);
        return mapToResponse(enrollment);
    }

    // ==================== CHECK ENROLLMENT EXISTS ====================

    public boolean isEnrolled(Long studentId, Long courseId) {
        return enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
    }

    // ==================== WISHLIST ====================

    public WishListResponse addToWishList(Long studentId, Long courseId) {
        if (wishListRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new RuntimeException("Course is already in your wishlist");
        }
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new RuntimeException("You are already enrolled in this course");
        }

        WishList wishList = WishList.builder()
                .studentId(studentId)
                .courseId(courseId)
                .build();

        wishList = wishListRepository.save(wishList);
        return mapToWishListResponse(wishList);
    }

    public List<WishListResponse> getWishList(Long studentId) {
        return wishListRepository.findByStudentId(studentId)
                .stream()
                .map(this::mapToWishListResponse)
                .collect(Collectors.toList());
    }

    public String removeFromWishList(Long studentId, Long courseId) {
        WishList wishList = wishListRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new RuntimeException("Course not found in wishlist"));
        wishListRepository.delete(wishList);
        return "Course removed from wishlist";
    }

    // ==================== INTER-SERVICE CALLS ====================

    private void verifyStudentExists(Long studentId) {
        try {
            String url = studentServiceUrl + "/api/students/" + studentId + "/exists";
            Map response = restTemplate.getForObject(url, Map.class);
            if (response == null || !Boolean.TRUE.equals(response.get("exists"))) {
                throw new RuntimeException("Student not found with ID: " + studentId);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Student Service unavailable: " + e.getMessage());
        }
    }

    private void verifyCourseExists(Long courseId) {
        try {
            String url = courseServiceUrl + "/api/courses/" + courseId + "/exists";
            Map response = restTemplate.getForObject(url, Map.class);
            if (response == null || !Boolean.TRUE.equals(response.get("exists"))) {
                throw new RuntimeException("Course not found with ID: " + courseId);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Course Service unavailable: " + e.getMessage());
        }
    }

    // ==================== MAPPERS ====================

    private EnrollmentResponse mapToResponse(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .enrollmentId(enrollment.getEnrollmentId())
                .studentId(enrollment.getStudentId())
                .courseId(enrollment.getCourseId())
                .enrolledAt(enrollment.getEnrolledAt())
                .status(enrollment.getStatus().name())
                .completedAt(enrollment.getCompletedAt())
                .paymentId(enrollment.getPaymentId())
                .amountPaid(enrollment.getAmountPaid())
                .build();
    }

    private WishListResponse mapToWishListResponse(WishList wishList) {
        return WishListResponse.builder()
                .wishlistId(wishList.getWishlistId())
                .studentId(wishList.getStudentId())
                .courseId(wishList.getCourseId())
                .addedAt(wishList.getAddedAt())
                .build();
    }
}
