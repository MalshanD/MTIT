package com.onlinelearning.certificate.service;

import com.onlinelearning.certificate.dto.CertificateResponse;
import com.onlinelearning.certificate.entity.Certificate;
import com.onlinelearning.certificate.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final RestTemplate restTemplate;

    @Value("${services.student.url}")
    private String studentServiceUrl;

    @Value("${services.course.url}")
    private String courseServiceUrl;

    @Value("${services.instructor.url}")
    private String instructorServiceUrl;

    // ==================== ISSUE CERTIFICATE ====================
    // Called by Enrollment Service when course is completed

    public CertificateResponse issueCertificate(Long studentId, Long courseId) {

        // Check if certificate already issued
        if (certificateRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            Certificate existing = certificateRepository.findByStudentIdAndCourseId(studentId, courseId).get();
            return mapToResponse(existing);
        }

        // Fetch student name from Student Service
        String studentName = fetchStudentName(studentId);

        // Fetch course details from Course Service
        Map courseDetails = fetchCourseDetails(courseId);
        String courseName = (String) courseDetails.get("title");
        Long instructorId = Long.valueOf(courseDetails.get("instructorId").toString());

        // Generate unique certificate code
        String certCode = generateCertificateCode();

        Certificate certificate = Certificate.builder()
                .certificateCode(certCode)
                .studentId(studentId)
                .courseId(courseId)
                .instructorId(instructorId)
                .studentName(studentName)
                .courseName(courseName)
                .certificateUrl("/api/certificates/download/" + certCode)
                .isValid(true)
                .build();

        certificate = certificateRepository.save(certificate);
        return mapToResponse(certificate);
    }

    // ==================== GET CERTIFICATES BY STUDENT ====================

    public List<CertificateResponse> getCertificatesByStudent(Long studentId) {
        return certificateRepository.findByStudentId(studentId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ==================== VERIFY CERTIFICATE ====================

    public CertificateResponse verifyCertificate(String certificateCode) {
        Certificate cert = certificateRepository.findByCertificateCode(certificateCode)
                .orElseThrow(() -> new RuntimeException("Certificate not found with code: " + certificateCode));
        return mapToResponse(cert);
    }

    // ==================== REVOKE CERTIFICATE ====================

    public String revokeCertificate(Long certificateId) {
        Certificate cert = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));
        cert.setIsValid(false);
        certificateRepository.save(cert);
        return "Certificate has been revoked";
    }

    // ==================== INTER-SERVICE CALLS ====================

    private String fetchStudentName(Long studentId) {
        try {
            String url = studentServiceUrl + "/api/students/" + studentId;
            Map response = restTemplate.getForObject(url, Map.class);
            if (response != null) {
                return response.get("firstName") + " " + response.get("lastName");
            }
            return "Unknown Student";
        } catch (Exception e) {
            return "Unknown Student";
        }
    }

    private Map fetchCourseDetails(Long courseId) {
        try {
            String url = courseServiceUrl + "/api/courses/" + courseId;
            Map response = restTemplate.getForObject(url, Map.class);
            if (response != null) return response;
            throw new RuntimeException("Course not found");
        } catch (Exception e) {
            throw new RuntimeException("Course Service unavailable: " + e.getMessage());
        }
    }

    // ==================== HELPERS ====================

    private String generateCertificateCode() {
        String year = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy"));
        long count = certificateRepository.count() + 1;
        return String.format("CERT-%s-%05d", year, count);
    }

    private CertificateResponse mapToResponse(Certificate cert) {
        return CertificateResponse.builder()
                .certificateId(cert.getCertificateId())
                .certificateCode(cert.getCertificateCode())
                .studentId(cert.getStudentId())
                .courseId(cert.getCourseId())
                .instructorId(cert.getInstructorId())
                .studentName(cert.getStudentName())
                .courseName(cert.getCourseName())
                .issuedAt(cert.getIssuedAt())
                .certificateUrl(cert.getCertificateUrl())
                .isValid(cert.getIsValid())
                .build();
    }
}
