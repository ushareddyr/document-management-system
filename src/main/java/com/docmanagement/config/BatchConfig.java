package com.docmanagement.config;

import com.docmanagement.batch.DocumentProcessor;
import com.docmanagement.batch.DocumentReader;
import com.docmanagement.batch.DocumentWriter;
import com.docmanagement.model.Document;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DocumentReader documentReader;
    private final DocumentProcessor documentProcessor;
    private final DocumentWriter documentWriter;

    @Bean
    public Job processDocumentsJob() {
        return new JobBuilder("processDocumentsJob", jobRepository)
                .start(processDocumentsStep())
                .build();
    }

    @Bean
    public Step processDocumentsStep() {
        return new StepBuilder("processDocumentsStep", jobRepository)
                .<Document, Document>chunk(10, transactionManager)
                .reader(documentReader)
                .processor(documentProcessor)
                .writer(documentWriter)
                .build();
    }
}
