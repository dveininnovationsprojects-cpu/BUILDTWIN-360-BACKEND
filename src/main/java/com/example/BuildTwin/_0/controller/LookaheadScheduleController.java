package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.lookahead.*;
import com.example.BuildTwin._0.model.CommitmentStatus;
import com.example.BuildTwin._0.service.LookaheadScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(
        name = "Look-Ahead Schedule & Weekly Work Planning (7-Day & 14-Day Lookahead)",
        description = "Endpoints for managing 7-Day & 14-Day Look-Ahead rolling schedules, dual-week execution plans, daily workload matrices, field constraint checklists, and weekly task commitments."
)
public class LookaheadScheduleController {

    private final LookaheadScheduleService lookaheadScheduleService;

    // =========================================================================
    // 14-DAY LOOK-AHEAD (TWO-WEEK / SPRINT) ENDPOINTS
    // =========================================================================

    @GetMapping("/api/v1/projects/{projectId}/lookahead/14-day")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get 14-Day (Two-Week / Sprint) Look-Ahead Schedule",
            description = "Generates a rolling 14-day lookahead schedule split into Week 1 (immediate commitment & daily execution) and Week 2 (make-ready lookahead & constraint screening), complete with 14-day continuous daily matrix and predecessor readiness."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "14-Day Look-Ahead Schedule retrieved successfully",
                    content = @Content(schema = @Schema(implementation = FourteenDayLookaheadResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponse<FourteenDayLookaheadResponse>> getFourteenDayLookaheadSchedule(
            @PathVariable Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) Long workPackageId,
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) String discipline,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "true") Boolean includeOverdue) {

        FourteenDayLookaheadResponse response = lookaheadScheduleService.getFourteenDayLookaheadSchedule(
                projectId, startDate, workPackageId, siteId, buildingId, discipline, status, includeOverdue
        );
        return ResponseEntity.ok(ApiResponse.success(response, "14-Day Look-Ahead Schedule retrieved successfully"));
    }

    @GetMapping("/api/v1/projects/{projectId}/lookahead/14-day/metrics")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get 14-Day Look-Ahead Metrics & Trend Comparison",
            description = "Retrieves consolidated 14-day KPIs with comparative breakdown between Week 1 (Immediate Commitment) and Week 2 (Make-Ready Lookahead), plus overall schedule health status."
    )
    public ResponseEntity<ApiResponse<FourteenDayMetricsResponse>> getFourteenDayMetrics(
            @PathVariable Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {

        FourteenDayMetricsResponse metrics = lookaheadScheduleService.getFourteenDayMetrics(projectId, startDate);
        return ResponseEntity.ok(ApiResponse.success(metrics, "14-Day Look-Ahead metrics retrieved successfully"));
    }

    @GetMapping("/api/v1/projects/{projectId}/lookahead/14-day/constraints")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get 14-Day Look-Ahead Constraints & Hindrance Summary",
            description = "Analyzes pending hindrances across the full 14-day window: incomplete predecessors, material shortages, drawing revisions, equipment breakdowns, or missing work permits."
    )
    public ResponseEntity<ApiResponse<LookaheadConstraintSummaryResponse>> getFourteenDayConstraints(
            @PathVariable Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {

        LookaheadConstraintSummaryResponse summary = lookaheadScheduleService.getFourteenDayConstraints(projectId, startDate);
        return ResponseEntity.ok(ApiResponse.success(summary, "14-Day Look-Ahead constraints summary retrieved successfully"));
    }

    // =========================================================================
    // STANDARD / 7-DAY ROLLING LOOK-AHEAD ENDPOINTS
    // =========================================================================

    @GetMapping("/api/v1/projects/{projectId}/lookahead")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Look-Ahead Schedule (7-Day / Custom Days)",
            description = "Generates a rolling look-ahead schedule (default 7 days) with summary KPIs, day-by-day workload distribution matrix, and detailed activity cards with predecessor readiness and constraint statuses."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Look-Ahead Schedule retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LookaheadScheduleResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponse<LookaheadScheduleResponse>> getLookaheadSchedule(
            @PathVariable Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(defaultValue = "7") Integer days,
            @RequestParam(required = false) Long workPackageId,
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) String discipline,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "true") Boolean includeOverdue) {

        LookaheadScheduleResponse response = lookaheadScheduleService.getLookaheadSchedule(
                projectId, startDate, days, workPackageId, siteId, buildingId, discipline, status, includeOverdue
        );
        return ResponseEntity.ok(ApiResponse.success(response, "Look-Ahead Schedule retrieved successfully"));
    }

    @GetMapping("/api/v1/projects/{projectId}/lookahead/metrics")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Look-Ahead Schedule Metrics & Readiness Score",
            description = "Calculates high-level execution KPIs, starting/finishing task counts, overdue backlog, and overall readiness score percentage for the look-ahead window."
    )
    public ResponseEntity<ApiResponse<LookaheadMetricsResponse>> getLookaheadMetrics(
            @PathVariable Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(defaultValue = "7") Integer days) {

        LookaheadMetricsResponse metrics = lookaheadScheduleService.getLookaheadMetrics(projectId, startDate, days);
        return ResponseEntity.ok(ApiResponse.success(metrics, "Look-Ahead metrics retrieved successfully"));
    }

    @GetMapping("/api/v1/projects/{projectId}/lookahead/constraints")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Look-Ahead Constraints & Hindrance Summary",
            description = "Analyzes pending hindrances blocking lookahead activities: incomplete predecessors, material shortages, drawing revisions, equipment breakdowns, or missing work permits."
    )
    public ResponseEntity<ApiResponse<LookaheadConstraintSummaryResponse>> getLookaheadConstraints(
            @PathVariable Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(defaultValue = "7") Integer days) {

        LookaheadConstraintSummaryResponse summary = lookaheadScheduleService.getLookaheadConstraints(projectId, startDate, days);
        return ResponseEntity.ok(ApiResponse.success(summary, "Look-Ahead constraints summary retrieved successfully"));
    }

    // =========================================================================
    // WEEKLY / SPRINT WORK PLAN COMMITMENTS ENDPOINTS
    // =========================================================================

    @PostMapping("/api/v1/projects/{projectId}/lookahead/commitments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('SITE_SUPERVISOR') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Create Weekly / Lookahead Work Plan Commitment",
            description = "Commits an activity to the lookahead execution plan with targeted quantities, allocated gang/crew, and constraint verification checklist."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Lookahead commitment created successfully",
                    content = @Content(schema = @Schema(implementation = LookaheadCommitmentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request dates or target"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project or Activity not found")
    })
    public ResponseEntity<ApiResponse<LookaheadCommitmentResponse>> createCommitment(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateLookaheadCommitmentRequest request,
            @Parameter(hidden = true) Authentication authentication) {

        LookaheadCommitmentResponse created = lookaheadScheduleService.createCommitment(projectId, request, authentication.getName());
        return new ResponseEntity<>(ApiResponse.created(created, "Lookahead weekly commitment created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/projects/{projectId}/lookahead/commitments")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List Commitments for Project",
            description = "Retrieves all commitments made for the project with optional filters by week start date and commitment status."
    )
    public ResponseEntity<ApiResponse<List<LookaheadCommitmentResponse>>> getCommitments(
            @PathVariable Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
            @RequestParam(required = false) CommitmentStatus status) {

        List<LookaheadCommitmentResponse> list = lookaheadScheduleService.getCommitments(projectId, weekStartDate, status);
        return ResponseEntity.ok(ApiResponse.success(list, "Lookahead commitments retrieved successfully"));
    }

    @GetMapping("/api/v1/lookahead/commitments/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get Lookahead Commitment by ID",
            description = "Retrieves details of a specific weekly work plan commitment and its constraint checklist."
    )
    public ResponseEntity<ApiResponse<LookaheadCommitmentResponse>> getCommitmentById(
            @PathVariable Long id) {

        LookaheadCommitmentResponse commitment = lookaheadScheduleService.getCommitmentById(id);
        return ResponseEntity.ok(ApiResponse.success(commitment, "Lookahead commitment retrieved successfully"));
    }

    @PutMapping("/api/v1/lookahead/commitments/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('SITE_ENGINEER') or hasRole('SITE_SUPERVISOR') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Update Weekly Commitment / Constraint Checklist",
            description = "Updates target quantities, actual executed quantities, constraint checklist flags, or blocker explanations on a commitment."
    )
    public ResponseEntity<ApiResponse<LookaheadCommitmentResponse>> updateCommitment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLookaheadCommitmentRequest request,
            @Parameter(hidden = true) Authentication authentication) {

        LookaheadCommitmentResponse updated = lookaheadScheduleService.updateCommitment(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Lookahead commitment updated successfully"));
    }

    @DeleteMapping("/api/v1/lookahead/commitments/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Delete Weekly Commitment",
            description = "Removes an activity commitment from the weekly work plan."
    )
    public ResponseEntity<ApiResponse<Void>> deleteCommitment(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {

        lookaheadScheduleService.deleteCommitment(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Lookahead commitment deleted successfully"));
    }
}
