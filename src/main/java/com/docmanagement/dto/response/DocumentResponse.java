package com.docmanagement.dto.response;

import com.docmanagement.model.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentResponse {

    private Long id;
    private String title;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String description;
    private UserSummaryResponse uploadedBy;
    private Set<String> keywords;
    private DocumentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
