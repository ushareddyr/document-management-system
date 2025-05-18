package com.docmanagement.batch;

import com.docmanagement.model.Document;
import com.docmanagement.model.DocumentStatus;
import com.docmanagement.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class DocumentReader implements ItemReader<Document> {

    private final DocumentRepository documentRepository;
    private final AtomicInteger counter = new AtomicInteger(0);
    private List<Document> pendingDocuments;

    @Override
    public Document read() {
        if (pendingDocuments == null) {
            Pageable pageable = PageRequest.of(0, 100);
            pendingDocuments = documentRepository.findByStatus(DocumentStatus.PENDING, pageable).getContent();
            counter.set(0);
        }

        if (counter.get() >= pendingDocuments.size()) {
            pendingDocuments = null;
            return null;
        }

        return pendingDocuments.get(counter.getAndIncrement());
    }
}
