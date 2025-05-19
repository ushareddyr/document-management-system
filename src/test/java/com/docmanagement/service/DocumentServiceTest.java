package com.docmanagement.service;

import com.docmanagement.config.RabbitMQConfig;
import com.docmanagement.dto.request.DocumentFilterRequest;
import com.docmanagement.dto.request.DocumentUploadRequest;
import com.docmanagement.dto.response.DocumentResponse;
import com.docmanagement.model.Document;
import com.docmanagement.model.DocumentStatus;
import com.docmanagement.model.User;
import com.docmanagement.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentServiceTest {

    @InjectMocks
    private DocumentService documentService;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job processDocumentsJob;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(testUser, null)
        );
    }

    @Test
    void uploadDocument_shouldStoreFileAndSendToQueue() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "Hello World".getBytes());
        DocumentUploadRequest request = DocumentUploadRequest.builder()
                .title("Test Doc")
                .description("Desc")
                .keywords(Set.of("java"))
                .build();

        Document document = Document.builder()
                .id(100L)
                .title("Test Doc")
                .description("Desc")
                .fileName("test.txt")
                .fileType("text/plain")
                .fileSize(file.getSize())
                .filePath("some/path/test.txt")
                .keywords(request.getKeywords())
                .uploadedBy(testUser)
                .status(DocumentStatus.PENDING)
                .build();

        when(documentRepository.save(any(Document.class))).thenReturn(document);

        DocumentResponse response = documentService.uploadDocument(file, request);

        assertNotNull(response);
        assertEquals("Test Doc", response.getTitle());
        verify(rabbitTemplate).convertAndSend(RabbitMQConfig.DOCUMENT_EXCHANGE, RabbitMQConfig.DOCUMENT_ROUTING_KEY, document.getId());
        verify(documentRepository).updateContentVector(document.getId());

        // Cleanup temp file
        Files.deleteIfExists(Path.of(document.getFilePath()));
    }

    @Test
    void getDocumentById_shouldReturnResponse() {
        Document doc = Document.builder()
                .id(1L)
                .title("Doc 1")
                .uploadedBy(testUser)
                .fileName("doc.txt")
                .fileType("text/plain")
                .fileSize(100L)
                .filePath("dummy/path")
                .status(DocumentStatus.PENDING)
                .build();

        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        DocumentResponse response = documentService.getDocumentById(1L);

        assertNotNull(response);
        assertEquals("Doc 1", response.getTitle());
    }

    @Test
    void getDocumentById_shouldThrowIfNotFound() {
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(RuntimeException.class, () -> documentService.getDocumentById(1L));
        assertEquals("Document not found with id: 1", ex.getMessage());
    }

    @Test
    void getDocuments_shouldReturnPagedResponseByKeyword() {
        DocumentFilterRequest request = new DocumentFilterRequest();
        request.setKeyword("java");
        request.setSortBy("createdAt");
        request.setSortDirection("desc");
        request.setPage(0);
        request.setSize(10);

        Document doc = Document.builder()
                .id(1L)
                .title("Java Basics")
                .uploadedBy(testUser)
                .filePath("test.txt")
                .fileType("text/plain")
                .fileSize(100L)
                .status(DocumentStatus.PENDING)
                .build();

        Page<Document> docPage = new PageImpl<>(Collections.singletonList(doc));
        when(documentRepository.findByKeyword(eq("java"), any(Pageable.class))).thenReturn(docPage);

        var paged = documentService.getDocuments(request);

        assertNotNull(paged);
        assertEquals(1, paged.getContent().size());
        assertEquals("Java Basics", paged.getContent().get(0).getTitle());
    }

    @Test
    void deleteDocument_shouldRemoveDocumentAndFile() throws IOException {
        Document doc = Document.builder()
                .id(1L)
                .filePath("delete-test.txt")
                .build();

        Files.createFile(Path.of("delete-test.txt"));
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        documentService.deleteDocument(1L);

        verify(documentRepository).delete(doc);
        assertFalse(Files.exists(Path.of("delete-test.txt")));
    }

    @Test
    void processDocumentsBatch_shouldRunJob() throws Exception {
        documentService.processDocumentsBatch();

        verify(jobLauncher).run(eq(processDocumentsJob), any());
    }
}
