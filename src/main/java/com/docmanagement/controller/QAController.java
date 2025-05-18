package com.docmanagement.controller;

import com.docmanagement.dto.request.QuestionRequest;
import com.docmanagement.dto.response.AnswerResponse;
import com.docmanagement.service.QAService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/qa")
@RequiredArgsConstructor
@Tag(name = "Q&A", description = "Question and Answer API")
@SecurityRequirement(name = "Bearer Authentication")
public class QAController {

    private final QAService qaService;

    @PostMapping("/ask")
    @Operation(summary = "Ask a question and get relevant document snippets")
    public ResponseEntity<AnswerResponse> askQuestion(@Valid @RequestBody QuestionRequest questionRequest) {
        AnswerResponse response = qaService.answerQuestion(questionRequest);
        return ResponseEntity.ok(response);
    }
}
