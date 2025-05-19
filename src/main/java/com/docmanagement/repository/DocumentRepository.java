package com.docmanagement.repository;

import com.docmanagement.model.Document;
import com.docmanagement.model.DocumentStatus;
import com.docmanagement.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, CustomDocumentRepository  {

    Page<Document> findByUploadedBy(User user, Pageable pageable);
    
    Page<Document> findByStatus(DocumentStatus status, Pageable pageable);

    @Query(value = "SELECT d FROM Document d WHERE " +
            "(:title IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:fileType IS NULL OR d.fileType = :fileType) AND " +
            "(:uploadedById IS NULL OR d.uploadedBy.id = :uploadedById) AND " +
            "(:status IS NULL OR d.status = :status) AND " +
            "(:createdAfter IS NULL OR d.createdAt >= :createdAfter) AND " +
            "(:createdBefore IS NULL OR d.createdAt <= :createdBefore)",
            countQuery = "SELECT COUNT(d) FROM Document d WHERE " +
                    "(:title IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
                    "(:fileType IS NULL OR d.fileType = :fileType) AND " +
                    "(:uploadedById IS NULL OR d.uploadedBy.id = :uploadedById) AND " +
                    "(:status IS NULL OR d.status = :status) AND " +
                    "(:createdAfter IS NULL OR d.createdAt >= :createdAfter) AND " +
                    "(:createdBefore IS NULL OR d.createdAt <= :createdBefore)", nativeQuery = false)
    Page<Document> findByFilters(
            @Param("title") String title,
            @Param("fileType") String fileType,
            @Param("uploadedById") Long uploadedById,
            @Param("status") DocumentStatus status,
            @Param("createdAfter") LocalDateTime createdAfter,
            @Param("createdBefore") LocalDateTime createdBefore,
            Pageable pageable);
    
    @Query("SELECT d FROM Document d JOIN d.keywords k WHERE LOWER(k) = LOWER(:keyword)")
    Page<Document> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query(value = "SELECT d.* FROM documents d " +
                  "WHERE to_tsvector('english', d.content) @@ plainto_tsquery('english', :query) " +
                  "ORDER BY ts_rank(to_tsvector('english', d.content), plainto_tsquery('english', :query)) DESC",
           nativeQuery = true)
    List<Document> searchByContentFullText(@Param("query") String query, Pageable pageable);
    
    @Query(value = "SELECT d.* FROM documents d " +
                  "WHERE d.content ILIKE %:query% " +
                  "ORDER BY d.created_at DESC",
           nativeQuery = true)
    List<Document> searchByContentSimple(@Param("query") String query, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "UPDATE documents SET content_vector = to_tsvector('english', content) WHERE id = :id", nativeQuery = true)
    void updateContentVector(@Param("id") Long id);
}
