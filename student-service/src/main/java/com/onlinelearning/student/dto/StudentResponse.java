package com.onlinelearning.student.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponse {

    private Long studentId;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String profilePicture;
    private String bio;
    private LocalDateTime registeredAt;
    private Boolean isActive;
}
