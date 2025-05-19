package com.docmanagement.service;

import com.docmanagement.dto.request.QuestionRequest;
import com.docmanagement.dto.response.AnswerResponse;
import com.docmanagement.dto.response.DocumentSnippetResponse;
import com.docmanagement.model.Document;
import com.docmanagement.model.DocumentChunk;
import com.docmanagement.repository.DocumentChunkRepository;
import com.docmanagement.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QAServiceTest {

    private DocumentRepository documentRepository;
    private DocumentChunkRepository documentChunkRepository;
    private QAService qaService;

    @BeforeEach
    void setUp() {
        documentRepository = mock(DocumentRepository.class);
        documentChunkRepository = mock(DocumentChunkRepository.class);
        qaService = new QAService(documentRepository, documentChunkRepository);
    }

    @Test
    void testAnswerQuestion_FromDocumentChunks() {
        String question = "What is the capital of France?";
        QuestionRequest request = new QuestionRequest(question);

        Document document = Document.builder()
                .id(1L)
                .title("Geography")
                .build();

        DocumentChunk chunk = DocumentChunk.builder()
                .content("The capital of France is Paris.")
                .document(document)
                .build();

        when(documentChunkRepository.searchByContentFullText(eq(question), any(PageRequest.class)))
                .thenReturn(List.of(chunk));

        AnswerResponse response = qaService.answerQuestion(request);

        assertEquals(question, response.getQuestion());
        assertEquals(1, response.getRelevantDocuments().size());

        DocumentSnippetResponse snippet = response.getRelevantDocuments().get(0);
        assertEquals(document.getId(), snippet.getDocumentId());
        assertTrue(snippet.getSnippet().contains("Paris"));
    }

    @Test
    void testAnswerQuestion_FallbackToFullDocumentSearch() {
        String question = "Define machine learning";
        QuestionRequest request = new QuestionRequest(question);

        when(documentChunkRepository.searchByContentFullText(eq(question), any(PageRequest.class)))
                .thenThrow(RuntimeException.class);

        when(documentChunkRepository.searchByContentSimple(eq(question), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        Document document = Document.builder()
                .id(2L)
                .title("AI Fundamentals")
                .content("Machine learning is a subset of AI that allows systems to learn from data.")
                .build();

        when(documentRepository.searchByContentFullText(eq(question), any(PageRequest.class)))
                .thenReturn(List.of(document));

        AnswerResponse response = qaService.answerQuestion(request);

        assertEquals(question, response.getQuestion());
        assertEquals(1, response.getRelevantDocuments().size());
        assertTrue(response.getRelevantDocuments().get(0).getSnippet().contains("Machine learning"));
    }

    @Test
    void testAnswerQuestion_NoResults() {
        String question = "Nonexistent topic";
        QuestionRequest request = new QuestionRequest(question);

        when(documentChunkRepository.searchByContentFullText(eq(question), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(documentChunkRepository.searchByContentSimple(eq(question), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(documentRepository.searchByContentFullText(eq(question), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        when(documentRepository.searchByContentSimple(eq(question), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        AnswerResponse response = qaService.answerQuestion(request);

        assertEquals(question, response.getQuestion());
        assertTrue(response.getRelevantDocuments().isEmpty());
    }
}
