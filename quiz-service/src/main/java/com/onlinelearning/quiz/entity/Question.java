package com.onlinelearning.quiz.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @Column(nullable = false)
    private Long quizId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(nullable = false, name = "option_a")
    @JsonProperty("optionA")
    private String optionA;

    @Column(nullable = false, name = "option_b")
    @JsonProperty("optionB")
    private String optionB;

    @Column(nullable = false, name = "option_c")
    @JsonProperty("optionC")
    private String optionC;

    @Column(nullable = false, name = "option_d")
    @JsonProperty("optionD")
    private String optionD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private CorrectOption correctOption;

    @Column(nullable = false)
    private Integer marks = 1;

    @Column(nullable = false)
    private Integer orderIndex = 0;
}
