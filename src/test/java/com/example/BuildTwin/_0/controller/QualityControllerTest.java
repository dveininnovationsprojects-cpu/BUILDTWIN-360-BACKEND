package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.quality.dto.QualityResolutionDto;
import com.example.BuildTwin._0.domain.quality.dto.QualitySummaryDto;
import com.example.BuildTwin._0.domain.quality.dto.QualityVerificationDto;
import com.example.BuildTwin._0.domain.quality.model.QualityEvidence;
import com.example.BuildTwin._0.domain.quality.model.QualityIssue;
import com.example.BuildTwin._0.domain.quality.service.QualityService;
import com.example.BuildTwin._0.security.JwtAuthenticationFilter;
import com.example.BuildTwin._0.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = QualityController.class)
@AutoConfigureMockMvc(addFilters = false)
class QualityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QualityService qualityService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/v1/quality/issues - Record Quality Defect / Snag")
    void testCreateQualityIssue() throws Exception {
        QualityIssue input = QualityIssue.builder()
                .projectId(100L)
                .title("Cracked beam surface")
                .category("CONCRETE")
                .severity("HIGH")
                .responsibleParty("Contractor A")
                .dueDate(LocalDate.of(2026, 11, 20))
                .build();

        QualityIssue saved = QualityIssue.builder()
                .id(1L)
                .projectId(100L)
                .title("Cracked beam surface")
                .category("CONCRETE")
                .severity("HIGH")
                .status("OPEN")
                .responsibleParty("Contractor A")
                .dueDate(LocalDate.of(2026, 11, 20))
                .build();

        when(qualityService.createQualityIssue(any(QualityIssue.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/quality/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.title").value("Cracked beam surface"))
                .andExpect(jsonPath("$.data.severity").value("HIGH"));
    }

    @Test
    @DisplayName("GET /api/v1/quality/issues/{id} - Get Quality Issue By ID")
    void testGetQualityIssueById() throws Exception {
        QualityIssue issue = QualityIssue.builder()
                .id(1L)
                .projectId(100L)
                .title("Honeycombing")
                .category("CONCRETE")
                .severity("MEDIUM")
                .status("OPEN")
                .build();

        when(qualityService.getQualityIssueById(1L)).thenReturn(issue);

        mockMvc.perform(get("/api/v1/quality/issues/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.category").value("CONCRETE"));
    }

    @Test
    @DisplayName("POST /api/v1/quality/issues/{id}/resolve - Submit Rectification Resolution")
    void testResolveQualityIssue() throws Exception {
        QualityResolutionDto dto = QualityResolutionDto.builder()
                .resolutionNotes("Grouting complete and cured for 48 hours.")
                .resolvedBy("Site Engineer Kumar")
                .build();

        QualityIssue resolved = QualityIssue.builder()
                .id(1L)
                .projectId(100L)
                .title("Honeycombing")
                .category("CONCRETE")
                .status("RECTIFICATION_SUBMITTED")
                .resolutionNotes("Grouting complete and cured for 48 hours.")
                .resolvedBy("Site Engineer Kumar")
                .build();

        when(qualityService.resolveQualityIssue(eq(1L), any(QualityResolutionDto.class))).thenReturn(resolved);

        mockMvc.perform(post("/api/v1/quality/issues/1/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("RECTIFICATION_SUBMITTED"))
                .andExpect(jsonPath("$.data.resolvedBy").value("Site Engineer Kumar"));
    }

    @Test
    @DisplayName("POST /api/v1/quality/issues/{id}/verify - QA Verification & Closure")
    void testVerifyQualityIssue() throws Exception {
        QualityVerificationDto dto = QualityVerificationDto.builder()
                .verifiedBy("QA Lead Ramesh")
                .approved(true)
                .remarks("Defect satisfactorily rectified.")
                .build();

        QualityIssue verified = QualityIssue.builder()
                .id(1L)
                .projectId(100L)
                .title("Honeycombing")
                .category("CONCRETE")
                .status("CLOSED")
                .verifiedBy("QA Lead Ramesh")
                .build();

        when(qualityService.verifyQualityIssue(eq(1L), any(QualityVerificationDto.class))).thenReturn(verified);

        mockMvc.perform(post("/api/v1/quality/issues/1/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CLOSED"))
                .andExpect(jsonPath("$.data.verifiedBy").value("QA Lead Ramesh"));
    }

    @Test
    @DisplayName("GET /api/v1/quality/issues/{id}/evidence - Get Defect & Rectification Evidence Photos")
    void testGetEvidencePhotos() throws Exception {
        QualityEvidence photo = QualityEvidence.builder()
                .id(5L)
                .qualityIssueId(1L)
                .objectUrl("https://buildtwin.s3/defect.jpg")
                .stage("DEFECT_CAPTURE")
                .caption("Initial capture")
                .build();

        when(qualityService.getEvidenceByQualityIssueId(1L)).thenReturn(List.of(photo));

        mockMvc.perform(get("/api/v1/quality/issues/1/evidence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].objectUrl").value("https://buildtwin.s3/defect.jpg"));
    }

    @Test
    @DisplayName("GET /api/v1/quality/summary/project/{projectId} - Quality KPI Summary")
    void testGetQualitySummary() throws Exception {
        QualitySummaryDto summary = QualitySummaryDto.builder()
                .projectId(100L)
                .totalIssues(15)
                .openSnags(6)
                .openNcrs(2)
                .pendingVerification(3)
                .closedCount(4)
                .criticalCount(2)
                .highCount(4)
                .build();

        when(qualityService.getQualitySummary(100L)).thenReturn(summary);

        mockMvc.perform(get("/api/v1/quality/summary/project/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalIssues").value(15))
                .andExpect(jsonPath("$.data.openSnags").value(6))
                .andExpect(jsonPath("$.data.openNcrs").value(2));
    }
}
