package com.docmanagement.listener;

import com.docmanagement.config.RabbitMQConfig;
import com.docmanagement.model.Document;
import com.docmanagement.model.DocumentStatus;
import com.docmanagement.repository.DocumentRepository;
import com.docmanagement.service.DocumentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQListener {

    private final DocumentRepository documentRepository;
    private final DocumentProcessingService documentProcessingService;

    @RabbitListener(queues = RabbitMQConfig.DOCUMENT_QUEUE)
    @Transactional
    public void processDocument(Long documentId) {
        log.info("Received document ID: {}", documentId);
        
        Document document = documentRepository.findById(documentId)
                .orElse(null);
        
        if (document == null) {
            log.error("Document not found with ID: {}", documentId);
            return;
        }
        
        try {
            document.setStatus(DocumentStatus.PROCESSING);
            documentRepository.save(document);
            
            String content = documentProcessingService.extractContent(document.getFilePath());
            document.setContent(content);
            
            if (document.getKeywords() == null || document.getKeywords().isEmpty()) {
                document.setKeywords(documentProcessingService.extractKeywords(content));
            }
            
            document.setStatus(DocumentStatus.COMPLETED);
            documentRepository.save(document);
            
            log.info("Document processed successfully: {}", document.getTitle());
        } catch (Exception e) {
            log.error("Error processing document: {}", document.getTitle(), e);
            document.setStatus(DocumentStatus.FAILED);
            documentRepository.save(document);
        }
    }
}
