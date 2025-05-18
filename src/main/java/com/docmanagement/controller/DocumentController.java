package com.docmanagement.controller;

import com.docmanagement.dto.request.DocumentFilterRequest;
import com.docmanagement.dto.request.DocumentUploadRequest;
import com.docmanagement.dto.response.ApiResponse;
import com.docmanagement.dto.response.DocumentResponse;
import com.docmanagement.dto.response.PagedResponse;
import com.docmanagement.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Document Management API")
@SecurityRequirement(name = "Bearer Authentication")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a new document")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute DocumentUploadRequest request) throws IOException {
        DocumentResponse response = documentService.uploadDocument(file, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable Long id) {
        DocumentResponse response = documentService.getDocumentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get documents with filtering and pagination")
    public ResponseEntity<PagedResponse<DocumentResponse>> getDocuments(
            @ModelAttribute DocumentFilterRequest filterRequest) {
        PagedResponse<DocumentResponse> response = documentService.getDocuments(filterRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a document")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    public ResponseEntity<ApiResponse> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Document deleted successfully")
                .build());
    }

    @PostMapping("/process-batch")
    @Operation(summary = "Trigger batch processing of pending documents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> processDocumentsBatch() {
        documentService.processDocumentsBatch();
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Document batch processing triggered")
                .build());
    }
}
