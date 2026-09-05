package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.materials.dto.MaterialRequestDto;
import com.example.BuildTwin._0.domain.materials.model.Material;
import com.example.BuildTwin._0.domain.materials.service.MaterialService;
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
@RequestMapping("/api/v1/materials")
@RequiredArgsConstructor
@Tag(name = "4. Material Master Catalog", description = "Centralized material catalog, SKU management, standard units, rates & reorder alerts (FR-050)")
public class MaterialController {

    private final MaterialService materialService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Create Material Catalog Item (FR-050)", description = "Registers a new material with SKU code, category classification, standard unit, standard unit rate, and minimum reorder threshold.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Material>> createMaterial(@Valid @RequestBody MaterialRequestDto request) {
        Material material = materialService.createMaterial(request);
        return new ResponseEntity<>(ApiResponse.created(material, "Material created successfully"), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get All Materials", description = "Retrieves complete material catalog.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Material>>> getAllMaterials() {
        List<Material> materials = materialService.getAllMaterials();
        return ResponseEntity.ok(ApiResponse.success(materials, "Material catalog fetched successfully"));
    }

    @GetMapping("/code/{materialCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Material By SKU Code", description = "Retrieves material details by unique SKU code.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Material>> getMaterialByCode(@PathVariable String materialCode) {
        Material material = materialService.getMaterialByCode(materialCode);
        return ResponseEntity.ok(ApiResponse.success(material, "Material fetched successfully"));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Materials By Category", description = "Filter materials by category (CEMENT, STEEL, AGGREGATE, BRICKS, etc.).", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Material>>> getMaterialsByCategory(@PathVariable String category) {
        List<Material> materials = materialService.getMaterialsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(materials, "Materials filtered by category successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Update Material Catalog Details", description = "Updates category, unit, specification, standard rate, and reorder level for a material.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Material>> updateMaterial(@PathVariable Long id, @Valid @RequestBody com.example.BuildTwin._0.domain.materials.dto.MaterialUpdateDto request) {
        Material material = materialService.updateMaterial(id, request);
        return ResponseEntity.ok(ApiResponse.success(material, "Material updated successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Material By ID", description = "Retrieves material details by unique primary key ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Material>> getMaterialById(@PathVariable Long id) {
        Material material = materialService.getMaterialById(id);
        return ResponseEntity.ok(ApiResponse.success(material, "Material fetched successfully"));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Detect Low Stock Materials", description = "Identifies all materials where current stock balance is less than or equal to reorder level.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Material>>> getLowStockMaterials() {
        List<Material> materials = materialService.getLowStockMaterials();
        return ResponseEntity.ok(ApiResponse.success(materials, "Low stock materials fetched successfully"));
    }

    @GetMapping("/reorder-alerts")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Reorder Threshold Alerts", description = "Retrieves all materials where current stock level is less than or equal to minimum reorder threshold.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Material>>> getReorderAlerts() {
        List<Material> materials = materialService.getMaterialsNeedingReorder();
        return ResponseEntity.ok(ApiResponse.success(materials, "Reorder alerts fetched successfully"));
    }
}
