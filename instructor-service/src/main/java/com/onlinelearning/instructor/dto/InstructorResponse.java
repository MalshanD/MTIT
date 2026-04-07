package com.onlinelearning.instructor.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorResponse {

    private Long instructorId;
    private String firstName;
    private String lastName;
    private String email;
    private String bio;
    private String expertise;
    private String profilePicture;
    private String linkedinProfile;
    private String websiteUrl;
    private Integer totalCourses;
    private Integer totalStudents;
    private Double rating;
    private Boolean isVerified;
    private LocalDateTime joinedAt;
}
