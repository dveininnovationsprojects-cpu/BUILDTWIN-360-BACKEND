package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.projects.model.*;
import com.example.BuildTwin._0.domain.projects.service.ProjectService;
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
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "8. Project & Site Hierarchy Master", description = "Project Master, Site Details, Building/Tower, Floor/Level & Zone Hierarchy (FR-010 - FR-014)")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Create Construction Project (FR-010)", description = "Creates a new project record with code, name, locality, client info, dates, and contract value.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Project>> createProject(@Valid @RequestBody Project project) {
        Project created = projectService.createProject(project);
        return new ResponseEntity<>(ApiResponse.created(created, "Project created successfully"), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'STORE_KEEPER', 'QUALITY_ENGINEER', 'SAFETY_OFFICER', 'EXECUTIVE')")
    @Operation(summary = "Get All Projects", description = "Retrieves directory of all active and archived projects.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Project>>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        return ResponseEntity.ok(ApiResponse.success(projects, "Projects fetched successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'STORE_KEEPER', 'QUALITY_ENGINEER', 'SAFETY_OFFICER', 'EXECUTIVE')")
    @Operation(summary = "Get Project By ID", description = "Retrieves project master details by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Project>> getProjectById(@PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        return ResponseEntity.ok(ApiResponse.success(project, "Project details fetched successfully"));
    }

    @PostMapping("/{projectId}/buildings")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Add Building/Tower (FR-013)", description = "Creates a building or tower within a project.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Building>> addBuilding(@PathVariable Long projectId, @Valid @RequestBody Building building) {
        building.setProjectId(projectId);
        Building created = projectService.addBuilding(building);
        return new ResponseEntity<>(ApiResponse.created(created, "Building added successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/{projectId}/buildings")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'STORE_KEEPER', 'QUALITY_ENGINEER', 'SAFETY_OFFICER', 'EXECUTIVE')")
    @Operation(summary = "Get Buildings By Project", description = "Retrieves all towers/buildings for a project.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Building>>> getBuildingsByProject(@PathVariable Long projectId) {
        List<Building> buildings = projectService.getBuildingsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(buildings, "Buildings fetched successfully"));
    }

    @PostMapping("/buildings/{buildingId}/levels")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Add Level/Floor (FR-013)", description = "Creates a floor or level within a building.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Level>> addLevel(@PathVariable Long buildingId, @Valid @RequestBody Level level) {
        level.setBuildingId(buildingId);
        Level created = projectService.addLevel(level);
        return new ResponseEntity<>(ApiResponse.created(created, "Level added successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/buildings/{buildingId}/levels")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'STORE_KEEPER', 'QUALITY_ENGINEER', 'SAFETY_OFFICER', 'EXECUTIVE')")
    @Operation(summary = "Get Levels By Building", description = "Retrieves all levels for a building.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Level>>> getLevelsByBuilding(@PathVariable Long buildingId) {
        List<Level> levels = projectService.getLevelsByBuilding(buildingId);
        return ResponseEntity.ok(ApiResponse.success(levels, "Levels fetched successfully"));
    }

    @PostMapping("/levels/{levelId}/zones")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Add Zone (FR-013)", description = "Creates a zone within a level.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Zone>> addZone(@PathVariable Long levelId, @Valid @RequestBody Zone zone) {
        zone.setLevelId(levelId);
        Zone created = projectService.addZone(zone);
        return new ResponseEntity<>(ApiResponse.created(created, "Zone added successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/levels/{levelId}/zones")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'STORE_KEEPER', 'QUALITY_ENGINEER', 'SAFETY_OFFICER', 'EXECUTIVE')")
    @Operation(summary = "Get Zones By Level", description = "Retrieves all zones for a level.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Zone>>> getZonesByLevel(@PathVariable Long levelId) {
        List<Zone> zones = projectService.getZonesByLevel(levelId);
        return ResponseEntity.ok(ApiResponse.success(zones, "Zones fetched successfully"));
    }
}
