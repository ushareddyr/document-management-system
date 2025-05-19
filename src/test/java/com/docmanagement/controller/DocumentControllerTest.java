package com.docmanagement.controller;

import com.docmanagement.dto.request.DocumentFilterRequest;
import com.docmanagement.dto.request.DocumentUploadRequest;
import com.docmanagement.dto.response.ApiResponse;
import com.docmanagement.dto.response.DocumentResponse;
import com.docmanagement.dto.response.PagedResponse;
import com.docmanagement.security.JwtTokenProvider;
import com.docmanagement.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DocumentController.class)
@AutoConfigureMockMvc(addFilters = false)class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private ObjectMapper objectMapper;

    private DocumentResponse documentResponse;
    private PagedResponse<DocumentResponse> pagedResponse;

    @BeforeEach
    void setup() {
        documentResponse = new DocumentResponse();
        documentResponse.setId(1L);
        documentResponse.setTitle("Test Document");
        documentResponse.setDescription("Test description");

        pagedResponse = new PagedResponse<>();
        pagedResponse.setContent(List.of(documentResponse));
        pagedResponse.setPage(0);
        pagedResponse.setSize(10);
        pagedResponse.setTotalElements(1);
        pagedResponse.setTotalPages(1);
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "EDITOR"})
    void testUploadDocument() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Sample content".getBytes()
        );

        DocumentUploadRequest request = new DocumentUploadRequest();
        request.setTitle("Test Document");
        request.setDescription("Test description");

        Mockito.when(documentService.uploadDocument(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(documentResponse);

        mockMvc.perform(multipart("/documents")
                .file(file)
                // Since @ModelAttribute, other fields must be sent as form params
                .param("title", request.getTitle())
                .param("description", request.getDescription())
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Document")));
    }

    @Test
    @WithMockUser
    void testGetDocumentById() throws Exception {
        Mockito.when(documentService.getDocumentById(1L)).thenReturn(documentResponse);

        mockMvc.perform(get("/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Document")));
    }

    @Test
    @WithMockUser
    void testGetDocumentsWithFilter() throws Exception {
        Mockito.when(documentService.getDocuments(ArgumentMatchers.any(DocumentFilterRequest.class)))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/documents")
                .param("title", "Test")
                .param("page", "0")
                .param("size", "10")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Test Document")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "EDITOR"})
    void testDeleteDocument() throws Exception {
        Mockito.doNothing().when(documentService).deleteDocument(1L);

        mockMvc.perform(delete("/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Document deleted successfully")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testProcessDocumentsBatch() throws Exception {
        Mockito.doNothing().when(documentService).processDocumentsBatch();

        mockMvc.perform(post("/documents/process-batch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Document batch processing triggered")));
    }
}
