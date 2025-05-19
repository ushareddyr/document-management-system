package com.docmanagement.repository;

import com.docmanagement.model.Document;
import com.docmanagement.dto.request.DocumentFilterRequest;
import com.docmanagement.repository.CustomDocumentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CustomDocumentRepositoryImpl implements CustomDocumentRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Document> findByFilters(DocumentFilterRequest request, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // === Main Query ===
        CriteriaQuery<Document> cq = cb.createQuery(Document.class);
        Root<Document> root = cq.from(Document.class);

        List<Predicate> predicates = buildPredicates(request, cb, root);
        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(root.get("createdAt")));

        TypedQuery<Document> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Document> resultList = query.getResultList();

        // === Count Query ===
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Document> countRoot = countQuery.from(Document.class);
        List<Predicate> countPredicates = buildPredicates(request, cb, countRoot);
        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));
        Long count = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, pageable, count);
    }

    private List<Predicate> buildPredicates(DocumentFilterRequest request, CriteriaBuilder cb, Root<Document> root) {
        List<Predicate> predicates = new ArrayList<>();

        if (request.getTitle() != null) {
            predicates.add(cb.like(cb.lower(root.get("title")), "%" + request.getTitle().toLowerCase() + "%"));
        }
        if (request.getFileType() != null) {
            predicates.add(cb.equal(root.get("fileType"), request.getFileType()));
        }
        if (request.getUploadedById() != null) {
            predicates.add(cb.equal(root.get("uploadedBy").get("id"), request.getUploadedById()));
        }
        if (request.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), request.getStatus()));
        }
        if (request.getCreatedAfter() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getCreatedAfter()));
        }
        if (request.getCreatedBefore() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getCreatedBefore()));
        }

        return predicates;
    }

}
