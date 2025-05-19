package com.docmanagement.service;

import org.apache.tika.exception.TikaException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DocumentProcessingServiceTest {

    private DocumentProcessingService documentProcessingService;
    private File tempFile;

    @BeforeEach
    void setUp() {
        documentProcessingService = new DocumentProcessingService();
    }

    @AfterEach
    void cleanUp() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void testExtractKeywords_withValidContent_shouldReturnKeywordsExcludingStopWords() {
        String content = "Java is a powerful programming language. Java and Spring Boot are widely used.";

        Set<String> keywords = documentProcessingService.extractKeywords(content);

        assertNotNull(keywords);
        assertTrue(keywords.contains("java"));
        assertTrue(keywords.contains("powerful"));
        assertTrue(keywords.contains("programming"));
        assertTrue(keywords.contains("language"));
        assertFalse(keywords.contains("is"));  // stop word
        assertFalse(keywords.contains("and")); // stop word
    }

    @Test
    void testExtractKeywords_withEmptyContent_shouldReturnEmptySet() {
        Set<String> keywords = documentProcessingService.extractKeywords("");

        assertNotNull(keywords);
        assertTrue(keywords.isEmpty());
    }

    @Test
    void testExtractContent_withValidFile_shouldReturnContent() throws IOException, TikaException {
        // Create a temporary file with text content
        tempFile = File.createTempFile("test-doc", ".txt");
        String expectedContent = "This is a test document containing some sample text.";
        Files.writeString(tempFile.toPath(), expectedContent);

        String extractedContent = documentProcessingService.extractContent(tempFile.getAbsolutePath());

        assertNotNull(extractedContent);
        assertTrue(extractedContent.contains("test document"));
    }

    @Test
    void testExtractContent_withInvalidFile_shouldThrowIOException() {
        String invalidFilePath = "non_existing_file_123.txt";

        assertThrows(IOException.class, () -> {
            documentProcessingService.extractContent(invalidFilePath);
        });
    }
}
