package com.docmanagement.repository;

import com.docmanagement.model.Document;
import com.docmanagement.dto.request.DocumentFilterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomDocumentRepository {
    Page<Document> findByFilters(DocumentFilterRequest request, Pageable pageable);
}
