package com.onlinelearning.quiz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuestionResponse {
    private Long questionId;
    private Long quizId;
    private String questionText;

    @JsonProperty("optionA")
    private String optionA;

    @JsonProperty("optionB")
    private String optionB;

    @JsonProperty("optionC")
    private String optionC;

    @JsonProperty("optionD")
    private String optionD;

    private String correctOption;
    private Integer marks;
    private Integer orderIndex;
}
