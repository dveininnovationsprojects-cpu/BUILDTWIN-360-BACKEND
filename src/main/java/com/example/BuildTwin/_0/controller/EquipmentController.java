package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.equipment.model.*;
import com.example.BuildTwin._0.domain.equipment.service.EquipmentService;
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
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
@Tag(name = "14. Equipment & Asset Usage", description = "Equipment Asset Register, Site Allocation, Usage Hours & Downtime Tracking (FR-100 - FR-102)")
public class EquipmentController {

    private final EquipmentService equipmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Register Equipment Asset (FR-100)", description = "Registers concrete mixers, vibrators, generators, scaffolding, cranes, etc.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Equipment>> registerEquipment(@Valid @RequestBody Equipment equipment) {
        Equipment registered = equipmentService.registerEquipment(equipment);
        return new ResponseEntity<>(ApiResponse.created(registered, "Equipment asset registered successfully"), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get All Equipment Assets", description = "Retrieves directory of all tracked site equipment.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Equipment>>> getAllEquipment() {
        List<Equipment> equipmentList = equipmentService.getAllEquipment();
        return ResponseEntity.ok(ApiResponse.success(equipmentList, "Equipment directory fetched successfully"));
    }

    @PostMapping("/usage")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Record Equipment Daily Usage (FR-101, FR-102)", description = "Captures site asset allocation, usage hours, and downtime hours.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<EquipmentUsage>> recordUsage(@Valid @RequestBody EquipmentUsage usage) {
        EquipmentUsage recorded = equipmentService.recordUsage(usage);
        return new ResponseEntity<>(ApiResponse.created(recorded, "Equipment usage recorded successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/usage/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Equipment Usage By Project", description = "Retrieves equipment usage and downtime logs for a project.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<EquipmentUsage>>> getUsageByProject(@PathVariable Long projectId) {
        List<EquipmentUsage> usageList = equipmentService.getUsageByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(usageList, "Equipment usage logs fetched successfully"));
    }
}
