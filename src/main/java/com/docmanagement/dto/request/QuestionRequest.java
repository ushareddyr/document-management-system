package com.docmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionRequest {

    @NotBlank(message = "Question is required")
    @Size(min = 3, max = 500, message = "Question must be between 3 and 500 characters")
    private String question;
}
