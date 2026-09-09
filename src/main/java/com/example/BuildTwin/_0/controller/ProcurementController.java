package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.procurement.model.*;
import com.example.BuildTwin._0.domain.procurement.service.ProcurementService;
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
@RequestMapping("/api/v1/procurement")
@RequiredArgsConstructor
@Tag(name = "11. Procurement & Goods Receipt (GRN)", description = "Material Requests, Purchase Orders, Delivery Verification & Goods Receipt Notes (FR-051, FR-052, FR-053, FR-061)")
public class ProcurementController {

    private final ProcurementService procurementService;

    @PostMapping("/requests")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Raise Material Request (FR-051)", description = "Creates site material purchase request for approval.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<MaterialRequest>> createMaterialRequest(@Valid @RequestBody MaterialRequest request) {
        MaterialRequest created = procurementService.createMaterialRequest(request);
        return new ResponseEntity<>(ApiResponse.created(created, "Material request raised successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/requests/{id}/approval")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Process Material Request Approval / Rejection (FR-051)", description = "Updates approval status (APPROVED, REJECTED) with metadata and rejection reason.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<MaterialRequest>> updateMaterialRequestApproval(
            @PathVariable Long id,
            @Valid @RequestBody com.example.BuildTwin._0.domain.procurement.dto.MaterialRequestApprovalDto approvalDto) {
        MaterialRequest updated = procurementService.updateMaterialRequestStatus(id, approvalDto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Material request approval status updated successfully"));
    }

    @GetMapping("/requests/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Material Request By ID", description = "Retrieves material request details by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<MaterialRequest>> getMaterialRequestById(@PathVariable Long id) {
        MaterialRequest request = procurementService.getMaterialRequestById(id);
        return ResponseEntity.ok(ApiResponse.success(request, "Material request fetched successfully"));
    }

    @DeleteMapping("/requests/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Delete Material Request", description = "Deletes a material request by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteMaterialRequest(@PathVariable Long id) {
        procurementService.deleteMaterialRequest(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Material request deleted successfully"));
    }

    @GetMapping("/requests/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Material Requests By Project", description = "Retrieves all material requests for a project.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<MaterialRequest>>> getRequestsByProject(@PathVariable Long projectId) {
        List<MaterialRequest> requests = procurementService.getMaterialRequestsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(requests, "Material requests fetched successfully"));
    }

    @GetMapping("/requests/project/{projectId}/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Material Requests By Status", description = "Retrieves material requests filtered by project and approval status (PENDING, APPROVED, REJECTED).", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<MaterialRequest>>> getRequestsByStatus(@PathVariable Long projectId, @PathVariable String status) {
        List<MaterialRequest> requests = procurementService.getMaterialRequestsByStatus(projectId, status);
        return ResponseEntity.ok(ApiResponse.success(requests, "Filtered material requests fetched successfully"));
    }

    @GetMapping("/requests/project/{projectId}/projected-shortage")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Detect Projected Material Shortage (FR-056)", description = "Calculates projected inventory shortages by matching active material requests against store balance.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<com.example.BuildTwin._0.domain.materials.dto.ProjectedShortageDto>>> getProjectedShortages(@PathVariable Long projectId) {
        List<com.example.BuildTwin._0.domain.materials.dto.ProjectedShortageDto> shortages = procurementService.detectProjectedShortage(projectId);
        return ResponseEntity.ok(ApiResponse.success(shortages, "Projected material shortages calculated successfully"));
    }

    @PostMapping("/purchase-orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Create Purchase Order (FR-052)", description = "Records vendor purchase order reference, supplier, amount, and expected delivery date.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<PurchaseOrder>> createPurchaseOrder(@Valid @RequestBody PurchaseOrder po) {
        PurchaseOrder created = procurementService.createPurchaseOrder(po);
        return new ResponseEntity<>(ApiResponse.created(created, "Purchase order created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/purchase-orders/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Purchase Order By ID", description = "Retrieves purchase order details by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<PurchaseOrder>> getPurchaseOrderById(@PathVariable Long id) {
        PurchaseOrder po = procurementService.getPurchaseOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(po, "Purchase order fetched successfully"));
    }

    @DeleteMapping("/purchase-orders/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Delete Purchase Order", description = "Deletes a purchase order by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deletePurchaseOrder(@PathVariable Long id) {
        procurementService.deletePurchaseOrder(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Purchase order deleted successfully"));
    }

    @GetMapping("/purchase-orders/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Purchase Orders By Project", description = "Retrieves all POs associated with a project.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<PurchaseOrder>>> getPurchaseOrdersByProject(@PathVariable Long projectId) {
        List<PurchaseOrder> pos = procurementService.getPurchaseOrdersByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(pos, "Purchase orders fetched successfully"));
    }

    @PostMapping("/grn")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Record Goods Receipt Note (GRN) (FR-053)", description = "Records received, accepted, and rejected quantities with delivery evidence photo.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Grn>> createGrn(@Valid @RequestBody Grn grn) {
        Grn created = procurementService.createGrn(grn);
        return new ResponseEntity<>(ApiResponse.created(created, "Goods Receipt Note (GRN) recorded successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/grn/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get GRN By ID", description = "Retrieves Goods Receipt Note details by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Grn>> getGrnById(@PathVariable Long id) {
        Grn grn = procurementService.getGrnById(id);
        return ResponseEntity.ok(ApiResponse.success(grn, "GRN details fetched successfully"));
    }

    @DeleteMapping("/grn/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Delete GRN Log", description = "Deletes a GRN record by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteGrn(@PathVariable Long id) {
        procurementService.deleteGrn(id);
        return ResponseEntity.ok(ApiResponse.success(null, "GRN log deleted successfully"));
    }

    @GetMapping("/grn/po/{poId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get GRN Log By Purchase Order", description = "Retrieves all GRN delivery receipts for a specific purchase order.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Grn>>> getGrnsByPo(@PathVariable Long poId) {
        List<Grn> grns = procurementService.getGrnsByPo(poId);
        return ResponseEntity.ok(ApiResponse.success(grns, "GRN receipts fetched successfully"));
    }
}
