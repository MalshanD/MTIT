package com.onlinelearning.auth.dto;

import com.onlinelearning.auth.entity.Question;
import lombok.Data;

@Data
public class QuestionDto {
    private Long questionId;
    private Long quizId;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private Question.CorrectOption correctOption;
    private Integer marks;
    private Integer orderIndex;
}
