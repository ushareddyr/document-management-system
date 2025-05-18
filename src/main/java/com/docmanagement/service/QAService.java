package com.docmanagement.service;

import com.docmanagement.dto.request.QuestionRequest;
import com.docmanagement.dto.response.AnswerResponse;
import com.docmanagement.dto.response.DocumentSnippetResponse;
import com.docmanagement.model.Document;
import com.docmanagement.model.DocumentChunk;
import com.docmanagement.repository.DocumentChunkRepository;
import com.docmanagement.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QAService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private static final int MAX_RESULTS = 5;

    @Cacheable(value = "questions", key = "#questionRequest.question")
    public AnswerResponse answerQuestion(QuestionRequest questionRequest) {
        String question = questionRequest.getQuestion();
        Pageable pageable = PageRequest.of(0, MAX_RESULTS);
        
        // Search for relevant document chunks
        List<DocumentChunk> relevantChunks;
        try {
            // Try full-text search first
            relevantChunks = documentChunkRepository.searchByContentFullText(question, pageable);
        } catch (Exception e) {
            // Fall back to simple search if full-text search fails
            relevantChunks = documentChunkRepository.searchByContentSimple(question, pageable);
        }
        
        // If no chunks found, search in full documents
        if (relevantChunks.isEmpty()) {
            List<Document> relevantDocuments;
            try {
                relevantDocuments = documentRepository.searchByContentFullText(question, pageable);
            } catch (Exception e) {
                relevantDocuments = documentRepository.searchByContentSimple(question, pageable);
            }
            
            List<DocumentSnippetResponse> snippets = relevantDocuments.stream()
                    .map(doc -> DocumentSnippetResponse.builder()
                            .documentId(doc.getId())
                            .documentTitle(doc.getTitle())
                            .snippet(extractSnippet(doc.getContent(), question))
                            .relevanceScore(1.0) // Simple relevance score
                            .build())
                    .collect(Collectors.toList());
            
            return AnswerResponse.builder()
                    .question(question)
                    .relevantDocuments(snippets)
                    .build();
        }
        
        // Process chunks to create snippets
        List<DocumentSnippetResponse> snippets = new ArrayList<>();
        for (DocumentChunk chunk : relevantChunks) {
            Document document = chunk.getDocument();
            
            DocumentSnippetResponse snippet = DocumentSnippetResponse.builder()
                    .documentId(document.getId())
                    .documentTitle(document.getTitle())
                    .snippet(extractSnippet(chunk.getContent(), question))
                    .relevanceScore(calculateRelevance(chunk.getContent(), question))
                    .build();
            
            snippets.add(snippet);
        }
        
        return AnswerResponse.builder()
                .question(question)
                .relevantDocuments(snippets)
                .build();
    }
    
    private String extractSnippet(String content, String question) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        // Simple snippet extraction - find a sentence containing keywords from the question
        String[] sentences = content.split("[.!?]");
        String[] questionWords = question.toLowerCase().split("\\s+");
        
        for (String sentence : sentences) {
            String sentenceLower = sentence.toLowerCase();
            for (String word : questionWords) {
                if (word.length() > 3 && sentenceLower.contains(word)) {
                    return sentence.trim() + ".";
                }
            }
        }
        
        // If no matching sentence found, return the first part of the content
        int snippetLength = Math.min(content.length(), 200);
        return content.substring(0, snippetLength) + "...";
    }
    
    private double calculateRelevance(String content, String question) {
        if (content == null || question == null) {
            return 0.0;
        }
        
        // Simple relevance calculation based on word overlap
        String[] contentWords = content.toLowerCase().split("\\s+");
        String[] questionWords = question.toLowerCase().split("\\s+");
        
        int matches = 0;
        for (String qWord : questionWords) {
            if (qWord.length() <= 3) continue; // Skip short words
            
            for (String cWord : contentWords) {
                if (cWord.contains(qWord) || qWord.contains(cWord)) {
                    matches++;
                    break;
                }
            }
        }
        
        return (double) matches / questionWords.length;
    }
}
