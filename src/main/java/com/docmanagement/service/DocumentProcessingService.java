package com.docmanagement.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DocumentProcessingService {

    private final Tika tika = new Tika();
    private final Set<String> stopWords = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "or", "but", "is", "are", "was", "were", 
            "be", "been", "being", "in", "on", "at", "to", "for", "with", "by", 
            "about", "against", "between", "into", "through", "during", "before", 
            "after", "above", "below", "from", "up", "down", "of", "off", "over", "under"
    ));

    public String extractContent(String filePath) throws IOException, TikaException {
        File file = new File(filePath);
        Metadata metadata = new Metadata();
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return tika.parseToString(inputStream, metadata);
        }
    }

    public Set<String> extractKeywords(String content) {
        if (content == null || content.isEmpty()) {
            return new HashSet<>();
        }
        
        // Simple keyword extraction by tokenizing and filtering
        String[] words = content.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", " ")
                .split("\\s+");
        
        // Count word frequencies
        return Arrays.stream(words)
                .filter(word -> word.length() > 3)
                .filter(word -> !stopWords.contains(word))
                .collect(Collectors.toSet());
    }
}
