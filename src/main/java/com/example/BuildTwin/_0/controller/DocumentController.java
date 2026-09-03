package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.documents.model.ProjectDocument;
import com.example.BuildTwin._0.domain.documents.service.DocumentService;
import com.example.BuildTwin._0.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "15. Document & Photo Repository", description = "Centralized project document management, version control & photo metadata (FR-110 - FR-114)")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'QUALITY_ENGINEER', 'SAFETY_OFFICER')")
    @Operation(summary = "Upload Document Metadata (FR-110, FR-111)", description = "Stores metadata for drawings, approvals, BOQs, quotations, reports, and invoice evidence.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProjectDocument>> uploadDocument(@Valid @RequestBody ProjectDocument document) {
        ProjectDocument uploaded = documentService.uploadDocument(document);
        return new ResponseEntity<>(ApiResponse.created(uploaded, "Document uploaded successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'QUALITY_ENGINEER', 'SAFETY_OFFICER', 'EXECUTIVE')")
    @Operation(summary = "Get Documents By Project (FR-113)", description = "Retrieves all project documents and versions.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<ProjectDocument>>> getDocumentsByProject(@PathVariable Long projectId) {
        List<ProjectDocument> documents = documentService.getDocumentsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(documents, "Project documents fetched successfully"));
    }

    @GetMapping("/project/{projectId}/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'QUALITY_ENGINEER', 'SAFETY_OFFICER', 'EXECUTIVE')")
    @Operation(summary = "Filter Documents By Category (FR-113)", description = "Filters project documents by category (DRAWING, APPROVAL, BOQ, etc.).", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<ProjectDocument>>> getDocumentsByCategory(@PathVariable Long projectId,
                                                                                       @PathVariable String category) {
        List<ProjectDocument> documents = documentService.getDocumentsByProjectAndCategory(projectId, category);
        return ResponseEntity.ok(ApiResponse.success(documents, "Category documents fetched successfully"));
    }
}
