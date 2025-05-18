package com.docmanagement.repository;

import com.docmanagement.model.Document;
import com.docmanagement.model.DocumentChunk;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findByDocumentOrderByChunkOrder(Document document);
    
    @Query(value = "SELECT dc.* FROM document_chunks dc " +
                  "WHERE to_tsvector('english', dc.content) @@ plainto_tsquery('english', :query) " +
                  "ORDER BY ts_rank(to_tsvector('english', dc.content), plainto_tsquery('english', :query)) DESC",
           nativeQuery = true)
    List<DocumentChunk> searchByContentFullText(@Param("query") String query, Pageable pageable);
    
    @Query(value = "SELECT dc.* FROM document_chunks dc " +
                  "WHERE dc.content ILIKE %:query% " +
                  "ORDER BY dc.id",
           nativeQuery = true)
    List<DocumentChunk> searchByContentSimple(@Param("query") String query, Pageable pageable);
}
