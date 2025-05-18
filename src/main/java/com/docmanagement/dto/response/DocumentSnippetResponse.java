package com.docmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentSnippetResponse {

    private Long documentId;
    private String documentTitle;
    private String snippet;
    private Double relevanceScore;
}
