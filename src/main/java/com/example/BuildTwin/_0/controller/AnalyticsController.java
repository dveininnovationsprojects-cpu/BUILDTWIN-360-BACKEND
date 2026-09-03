package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.analytics.dto.DelayRiskDto;
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
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "16. Construction Intelligence Analytics", description = "Project Health Index, Delay Risk Scoring, Schedule Slippage & Executive Metrics (Section 11)")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/project-health/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'PLANNING_ENGINEER', 'EXECUTIVE')")
    @Operation(summary = "Get Project Health Index (Section 11)", description = "Computes composite Project Health Index (0-100) from schedule, cost, quality, material, and critical blocker indicators.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ProjectHealthDto>> getProjectHealth(@PathVariable Long projectId) {
        ProjectHealthDto health = analyticsService.calculateProjectHealth(projectId);
        return ResponseEntity.ok(ApiResponse.success(health, "Project health index computed successfully"));
    }

    @GetMapping("/delay-risk/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'PLANNING_ENGINEER', 'EXECUTIVE')")
    @Operation(summary = "Get Delay Risk Score (Section 11)", description = "Computes weighted delay risk score based on predecessor slippage, labour readiness, and active site blockers.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<DelayRiskDto>> getDelayRisk(@PathVariable Long projectId) {
        DelayRiskDto delayRisk = analyticsService.calculateDelayRisk(projectId);
        return ResponseEntity.ok(ApiResponse.success(delayRisk, "Delay risk score computed successfully"));
    }
}
