package com.docmanagement.service;

import com.docmanagement.config.RabbitMQConfig;
import com.docmanagement.dto.request.DocumentFilterRequest;
import com.docmanagement.dto.request.DocumentUploadRequest;
import com.docmanagement.dto.response.DocumentResponse;
import com.docmanagement.dto.response.PagedResponse;
import com.docmanagement.dto.response.UserSummaryResponse;
import com.docmanagement.model.Document;
import com.docmanagement.model.DocumentStatus;
import com.docmanagement.model.User;
import com.docmanagement.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final JobLauncher jobLauncher;
    private final Job processDocumentsJob;
    private final String documentStorageLocation = "./document-storage";

    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file, DocumentUploadRequest request) throws IOException {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // Create storage directory if it doesn't exist
        Path storagePath = Paths.get(documentStorageLocation);
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        Path filePath = storagePath.resolve(uniqueFilename);
        
        // Save file to disk
        Files.copy(file.getInputStream(), filePath);
        
        // Create document entity
        Document document = Document.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .fileName(originalFilename)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .filePath(filePath.toString())
                .uploadedBy(currentUser)
                .keywords(request.getKeywords())
                .status(DocumentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        
        Document savedDocument = documentRepository.save(document);
        documentRepository.updateContentVector(savedDocument.getId());
        // Send to message queue for processing
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DOCUMENT_EXCHANGE,
                RabbitMQConfig.DOCUMENT_ROUTING_KEY,
                savedDocument.getId()
        );
        
        return mapToDocumentResponse(savedDocument);
    }

    @Async
    public void processDocumentsBatch() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            
            jobLauncher.run(processDocumentsJob, jobParameters);
        } catch (Exception e) {
            log.error("Error processing documents batch", e);
        }
    }

    @Cacheable(value = "documents", key = "#id")
    public DocumentResponse getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        return mapToDocumentResponse(document);
    }

    @Cacheable(value = "documents", key = "'filter:' + #filterRequest.hashCode()")
    public PagedResponse<DocumentResponse> getDocuments(DocumentFilterRequest filterRequest) {
        Sort sort = Sort.by(
                filterRequest.getSortDirection().equalsIgnoreCase("asc") ? 
                        Sort.Direction.ASC : Sort.Direction.DESC,
                filterRequest.getSortBy());
        
        Pageable pageable = PageRequest.of(filterRequest.getPage(), filterRequest.getSize(), sort);
        
        Page<Document> documentPage;
        
        if (filterRequest.getKeyword() != null && !filterRequest.getKeyword().isEmpty()) {
            documentPage = documentRepository.findByKeyword(filterRequest.getKeyword(), pageable);
        } else {
            documentPage = documentRepository.findByFilters(
                    filterRequest.getTitle(),
                    filterRequest.getFileType(),
                    filterRequest.getUploadedById(),
                    filterRequest.getStatus(),
                    filterRequest.getCreatedAfter(),
                    filterRequest.getCreatedBefore(),
                    pageable
            );
        }
        
        List<DocumentResponse> content = documentPage.getContent().stream()
                .map(this::mapToDocumentResponse)
                .collect(Collectors.toList());
        
        return PagedResponse.<DocumentResponse>builder()
                .content(content)
                .page(documentPage.getNumber())
                .size(documentPage.getSize())
                .totalElements(documentPage.getTotalElements())
                .totalPages(documentPage.getTotalPages())
                .last(documentPage.isLast())
                .build();
    }

    @CacheEvict(value = "documents", key = "#id")
    @Transactional
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        
        // Delete the physical file
        try {
            Files.deleteIfExists(Paths.get(document.getFilePath()));
        } catch (IOException e) {
            log.error("Error deleting document file: {}", document.getFilePath(), e);
        }
        
        documentRepository.delete(document);
    }

    private DocumentResponse mapToDocumentResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .fileName(document.getFileName())
                .fileType(document.getFileType())
                .fileSize(document.getFileSize())
                .description(document.getDescription())
                .uploadedBy(UserSummaryResponse.builder()
                        .id(document.getUploadedBy().getId())
                        .username(document.getUploadedBy().getUsername())
                        .firstName(document.getUploadedBy().getFirstName())
                        .lastName(document.getUploadedBy().getLastName())
                        .build())
                .keywords(document.getKeywords())
                .status(document.getStatus())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}
