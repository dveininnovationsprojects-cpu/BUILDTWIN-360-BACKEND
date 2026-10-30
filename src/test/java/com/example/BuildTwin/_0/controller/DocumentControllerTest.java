package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.documents.model.ProjectDocument;
import com.example.BuildTwin._0.domain.documents.service.DocumentService;
import com.example.BuildTwin._0.security.JwtAuthenticationFilter;
import com.example.BuildTwin._0.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DocumentService documentService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private ProjectDocument sampleDoc;

    @BeforeEach
    void setUp() {
        sampleDoc = ProjectDocument.builder()
                .id(1L)
                .projectId(100L)
                .title("Architectural Elevation Tower A")
                .category("DRAWING")
                .version("v1.0")
                .objectUrl("https://storage.buildtwin360.com/drawings/elev_tower_a_v1.pdf")
                .status("ACTIVE")
                .uploadedBy("Chief_Architect")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should successfully upload project document metadata (FR-110, FR-111)")
    void testUploadDocument() throws Exception {
        when(documentService.uploadDocument(any(ProjectDocument.class))).thenReturn(sampleDoc);

        mockMvc.perform(post("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDoc)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Document uploaded successfully"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.title").value("Architectural Elevation Tower A"))
                .andExpect(jsonPath("$.data.category").value("DRAWING"))
                .andExpect(jsonPath("$.data.version").value("v1.0"));

        verify(documentService, times(1)).uploadDocument(any(ProjectDocument.class));
    }

    @Test
    @DisplayName("Should fetch document metadata by ID")
    void testGetDocumentById() throws Exception {
        when(documentService.getDocumentById(1L)).thenReturn(sampleDoc);

        mockMvc.perform(get("/api/v1/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.projectId").value(100L))
                .andExpect(jsonPath("$.data.category").value("DRAWING"));

        verify(documentService, times(1)).getDocumentById(1L);
    }

    @Test
    @DisplayName("Should retrieve all documents for a project (FR-113)")
    void testGetDocumentsByProject() throws Exception {
        ProjectDocument doc2 = ProjectDocument.builder()
                .id(2L)
                .projectId(100L)
                .title("Structural Foundation BOQ")
                .category("BOQ")
                .version("v2.1")
                .objectUrl("https://storage.buildtwin360.com/boq/foundation_boq_v2.xlsx")
                .status("ACTIVE")
                .uploadedBy("QS_Engineer")
                .build();

        when(documentService.getDocumentsByProject(100L)).thenReturn(List.of(sampleDoc, doc2));

        mockMvc.perform(get("/api/v1/documents/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("Architectural Elevation Tower A"))
                .andExpect(jsonPath("$.data[1].title").value("Structural Foundation BOQ"));

        verify(documentService, times(1)).getDocumentsByProject(100L);
    }

    @Test
    @DisplayName("Should filter documents by project and category (FR-113)")
    void testGetDocumentsByCategory() throws Exception {
        when(documentService.getDocumentsByProjectAndCategory(100L, "DRAWING")).thenReturn(List.of(sampleDoc));

        mockMvc.perform(get("/api/v1/documents/project/100/category/DRAWING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].category").value("DRAWING"));

        verify(documentService, times(1)).getDocumentsByProjectAndCategory(100L, "DRAWING");
    }

    @Test
    @DisplayName("Should delete project document record by ID")
    void testDeleteDocument() throws Exception {
        doNothing().when(documentService).deleteDocument(1L);

        mockMvc.perform(delete("/api/v1/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Document deleted successfully"));

        verify(documentService, times(1)).deleteDocument(1L);
    }
}
