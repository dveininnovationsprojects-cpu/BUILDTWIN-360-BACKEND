package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.wbs.model.Activity;
import com.example.BuildTwin._0.domain.wbs.model.ActivityDependency;
import com.example.BuildTwin._0.domain.wbs.service.WbsService;
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
@RequestMapping("/api/v1/wbs")
@RequiredArgsConstructor
@Tag(name = "9. WBS, Activities & Schedule", description = "Work Breakdown Structure, Baseline Schedule, Dependencies & Look-ahead Planning (FR-020 - FR-025)")
public class WbsController {

    private final WbsService wbsService;

    @PostMapping("/activities")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'PLANNING_ENGINEER')")
    @Operation(summary = "Create WBS Activity (FR-020, FR-021)", description = "Creates a WBS activity storing discipline, unit, planned quantity, start/end dates, contractor, and weightage.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Activity>> createActivity(@Valid @RequestBody Activity activity) {
        Activity created = wbsService.createActivity(activity);
        return new ResponseEntity<>(ApiResponse.created(created, "Activity created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/projects/{projectId}/activities")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'EXECUTIVE')")
    @Operation(summary = "Get Activities By Project", description = "Retrieves all WBS activities for a given project.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Activity>>> getActivitiesByProject(@PathVariable Long projectId) {
        List<Activity> activities = wbsService.getActivitiesByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(activities, "Activities fetched successfully"));
    }

    @GetMapping("/projects/{projectId}/lookahead")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'EXECUTIVE')")
    @Operation(summary = "Get Look-ahead Activities (FR-024)", description = "Retrieves 7-day or 14-day look-ahead activity list for site execution.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Activity>>> getLookaheadActivities(@PathVariable Long projectId,
                                                                                 @RequestParam(defaultValue = "14") int days) {
        List<Activity> lookahead = wbsService.getLookaheadActivities(projectId, days);
        return ResponseEntity.ok(ApiResponse.success(lookahead, "Lookahead activities fetched successfully"));
    }

    @PostMapping("/dependencies")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'PLANNING_ENGINEER')")
    @Operation(summary = "Add Activity Dependency (FR-022)", description = "Establishes Finish-to-Start or other dependency links between activities.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ActivityDependency>> addDependency(@Valid @RequestBody ActivityDependency dependency) {
        ActivityDependency created = wbsService.addDependency(dependency);
        return new ResponseEntity<>(ApiResponse.created(created, "Dependency established successfully"), HttpStatus.CREATED);
    }
}
