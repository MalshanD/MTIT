package com.onlinelearning.quiz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class QuestionRequest {
    @NotBlank(message = "Question text is required") private String questionText;
    @JsonProperty("optionA") @NotBlank(message = "Option A is required") private String optionA;
    @JsonProperty("optionB") @NotBlank(message = "Option B is required") private String optionB;
    @JsonProperty("optionC") @NotBlank(message = "Option C is required") private String optionC;
    @JsonProperty("optionD") @NotBlank(message = "Option D is required") private String optionD;
    @NotBlank(message = "Correct option is required")
    @Pattern(regexp = "A|B|C|D", message = "Correct option must be A, B, C, or D")
    private String correctOption;
    @NotNull(message = "Marks is required") private Integer marks;
    @NotNull(message = "Order index is required") private Integer orderIndex;
}
