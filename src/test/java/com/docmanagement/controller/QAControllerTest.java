package com.docmanagement.controller;

import com.docmanagement.dto.request.QuestionRequest;
import com.docmanagement.dto.response.AnswerResponse;
import com.docmanagement.dto.response.DocumentSnippetResponse;
import com.docmanagement.security.JwtTokenProvider;
import com.docmanagement.service.QAService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = QAController.class)
@AutoConfigureMockMvc(addFilters = false)
class QAControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QAService qaService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void askQuestion_returnsAnswerResponse() throws Exception {
        QuestionRequest request = new QuestionRequest();
        request.setQuestion("What is Spring Boot?");

        DocumentSnippetResponse doc1 = DocumentSnippetResponse.builder()
                .documentId(1L)
                .documentTitle("Spring Boot Guide")
                .snippet("Spring Boot makes it easy to create stand-alone...")
                .relevanceScore(0.95)
                .build();

        DocumentSnippetResponse doc2 = DocumentSnippetResponse.builder()
                .documentId(2L)
                .documentTitle("Getting Started with Spring Boot")
                .snippet("Spring Boot simplifies development by...")
                .relevanceScore(0.92)
                .build();

        AnswerResponse mockResponse = AnswerResponse.builder()
                .question("What is Spring Boot?")
                .relevantDocuments(List.of(doc1, doc2))
                .build();

        Mockito.when(qaService.answerQuestion(Mockito.any(QuestionRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/qa/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question", is("What is Spring Boot?")))
                .andExpect(jsonPath("$.relevantDocuments", hasSize(2)))
                .andExpect(jsonPath("$.relevantDocuments[0].documentId", is(1)))
                .andExpect(jsonPath("$.relevantDocuments[0].documentTitle", is("Spring Boot Guide")))
                .andExpect(jsonPath("$.relevantDocuments[0].snippet", containsString("stand-alone")))
                .andExpect(jsonPath("$.relevantDocuments[0].relevanceScore", closeTo(0.95, 0.01)))
                .andExpect(jsonPath("$.relevantDocuments[1].documentId", is(2)))
                .andExpect(jsonPath("$.relevantDocuments[1].documentTitle", is("Getting Started with Spring Boot")))
                .andExpect(jsonPath("$.relevantDocuments[1].snippet", containsString("simplifies development")))
                .andExpect(jsonPath("$.relevantDocuments[1].relevanceScore", closeTo(0.92, 0.01)));
    }
}
