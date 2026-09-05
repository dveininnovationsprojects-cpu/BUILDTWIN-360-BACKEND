package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.analytics.dto.ProjectHealthDto;
import com.example.BuildTwin._0.domain.analytics.service.AnalyticsService;
import com.example.BuildTwin._0.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "17. Management Reports", description = "Weekly Project Review Reports, Management Summaries & Executive Exports (FR-134, FR-135)")
public class ReportController {

    private final AnalyticsService analyticsService;

    @GetMapping("/weekly/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Weekly Management Review Report (FR-135)", description = "Generates comprehensive weekly review report combining progress, cost, labour, material, quality, and risk summaries.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProjectHealthDto>> getWeeklyReport(@PathVariable Long projectId) {
        ProjectHealthDto report = analyticsService.calculateProjectHealth(projectId);
        return ResponseEntity.ok(ApiResponse.success(report, "Weekly project review report generated successfully"));
    }
}
