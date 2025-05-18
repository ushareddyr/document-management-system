package com.docmanagement.batch;

import com.docmanagement.model.Document;
import com.docmanagement.model.DocumentChunk;
import com.docmanagement.model.DocumentStatus;
import com.docmanagement.repository.DocumentChunkRepository;
import com.docmanagement.service.DocumentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentProcessor implements ItemProcessor<Document, Document> {

    private final DocumentProcessingService documentProcessingService;
    private final DocumentChunkRepository documentChunkRepository;
    private static final int CHUNK_SIZE = 1000;

    @Override
    public Document process(Document document) {
        try {
            log.info("Processing document: {}", document.getTitle());
            document.setStatus(DocumentStatus.PROCESSING);
            
            String content = documentProcessingService.extractContent(document.getFilePath());
            document.setContent(content);
            
            // Create document chunks
            List<DocumentChunk> chunks = createChunks(document, content);
            documentChunkRepository.saveAll(chunks);
            
            // Extract keywords if not provided
            if (document.getKeywords() == null || document.getKeywords().isEmpty()) {
                document.setKeywords(documentProcessingService.extractKeywords(content));
            }
            
            document.setStatus(DocumentStatus.COMPLETED);
            return document;
        } catch (Exception e) {
            log.error("Error processing document: {}", document.getTitle(), e);
            document.setStatus(DocumentStatus.FAILED);
            return document;
        }
    }
    
    private List<DocumentChunk> createChunks(Document document, String content) {
        List<DocumentChunk> chunks = new ArrayList<>();
        
        if (content.length() <= CHUNK_SIZE) {
            DocumentChunk chunk = DocumentChunk.builder()
                    .document(document)
                    .chunkOrder(0)
                    .content(content)
                    .build();
            chunks.add(chunk);
        } else {
            int chunkCount = (int) Math.ceil((double) content.length() / CHUNK_SIZE);
            for (int i = 0; i < chunkCount; i++) {
                int start = i * CHUNK_SIZE;
                int end = Math.min(start + CHUNK_SIZE, content.length());
                String chunkContent = content.substring(start, end);
                
                DocumentChunk chunk = DocumentChunk.builder()
                        .document(document)
                        .chunkOrder(i)
                        .content(chunkContent)
                        .build();
                chunks.add(chunk);
            }
        }
        
        return chunks;
    }
}
