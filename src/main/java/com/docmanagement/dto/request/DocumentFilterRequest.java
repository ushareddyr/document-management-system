package com.docmanagement.dto.request;

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
public class DocumentFilterRequest {

    private String title;
    private String keyword;
    private String fileType;
    private Long uploadedById;
    private DocumentStatus status;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
    private int page = 0;
    private int size = 10;
}
