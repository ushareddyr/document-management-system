package com.docmanagement.batch;

import com.docmanagement.model.Document;
import com.docmanagement.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentWriter implements ItemWriter<Document> {

    private final DocumentRepository documentRepository;

    @Override
    public void write(Chunk<? extends Document> documents) {
        log.info("Writing {} documents", documents.size());
        documentRepository.saveAll(documents.getItems());
    }
}
