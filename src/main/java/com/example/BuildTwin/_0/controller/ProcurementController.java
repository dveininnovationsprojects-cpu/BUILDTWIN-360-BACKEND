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

    @PostMapping({"/requests", "/material-requests"})
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE')")
    @Operation(summary = "Raise Material Request (FR-051)", description = "Creates site material purchase request for approval.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<MaterialRequest>> createMaterialRequest(@Valid @RequestBody MaterialRequest request) {
        MaterialRequest created = procurementService.createMaterialRequest(request);
        return new ResponseEntity<>(ApiResponse.created(created, "Material request raised successfully"), HttpStatus.CREATED);
    }

    @GetMapping({"/requests", "/material-requests"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get All Material Requests", description = "Retrieves all material requests across projects or filtered by optional projectId.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<MaterialRequest>>> getAllRequests(@RequestParam(required = false) Long projectId) {
        List<MaterialRequest> requests = projectId != null
                ? procurementService.getMaterialRequestsByProject(projectId)
                : procurementService.getAllMaterialRequests();
        return ResponseEntity.ok(ApiResponse.success(requests, "Material requests fetched successfully"));
    }

    @PutMapping({"/requests/{id}", "/material-requests/{id}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE')")
    @Operation(summary = "Update Material Request", description = "Updates an existing pending material request (e.g. quantity, required date, site, or remarks). Only PENDING requests can be edited.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<MaterialRequest>> updateMaterialRequest(@PathVariable Long id, @RequestBody MaterialRequest request) {
        MaterialRequest updated = procurementService.updateMaterialRequest(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Material request updated successfully"));
    }

    @PutMapping({"/requests/{id}/approval", "/material-requests/{id}/approval", "/requests/{id}/status", "/material-requests/{id}/status"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Process Material Request Approval / Rejection (FR-051)", description = "Updates approval status (APPROVED, REJECTED) with metadata and rejection reason.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<MaterialRequest>> updateMaterialRequestApproval(
            @PathVariable Long id,
            @Valid @RequestBody com.example.BuildTwin._0.domain.procurement.dto.MaterialRequestApprovalDto approvalDto) {
        MaterialRequest updated = procurementService.updateMaterialRequestStatus(id, approvalDto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Material request approval status updated successfully"));
    }

    @GetMapping({"/requests/{id}", "/material-requests/{id}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Material Request By ID", description = "Retrieves material request details by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<MaterialRequest>> getMaterialRequestById(@PathVariable Long id) {
        MaterialRequest request = procurementService.getMaterialRequestById(id);
        return ResponseEntity.ok(ApiResponse.success(request, "Material request fetched successfully"));
    }

    @DeleteMapping({"/requests/{id}", "/material-requests/{id}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Cancel Material Request", description = "Cancels a material request by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteMaterialRequest(@PathVariable Long id) {
        procurementService.deleteMaterialRequest(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Material request cancelled successfully"));
    }

    @GetMapping({"/requests/project/{projectId}", "/material-requests/project/{projectId}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Material Requests By Project", description = "Retrieves all material requests for a project.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<MaterialRequest>>> getRequestsByProject(@PathVariable Long projectId) {
        List<MaterialRequest> requests = procurementService.getMaterialRequestsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(requests, "Material requests fetched successfully"));
    }

    @GetMapping({"/requests/project/{projectId}/status/{status}", "/material-requests/project/{projectId}/status/{status}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Material Requests By Status", description = "Retrieves material requests filtered by project and approval status (PENDING, APPROVED, REJECTED).", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<MaterialRequest>>> getRequestsByStatus(@PathVariable Long projectId, @PathVariable String status) {
        List<MaterialRequest> requests = procurementService.getMaterialRequestsByStatus(projectId, status);
        return ResponseEntity.ok(ApiResponse.success(requests, "Filtered material requests fetched successfully"));
    }

    @GetMapping({"/requests/project/{projectId}/projected-shortage", "/requests/project/{projectId}/projected-shortages", "/material-requests/project/{projectId}/projected-shortages"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Detect Projected Material Shortage (FR-056)", description = "Calculates projected inventory shortages by matching active material requests against store balance.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<com.example.BuildTwin._0.domain.materials.dto.ProjectedShortageDto>>> getProjectedShortages(@PathVariable Long projectId) {
        List<com.example.BuildTwin._0.domain.materials.dto.ProjectedShortageDto> shortages = procurementService.detectProjectedShortage(projectId);
        return ResponseEntity.ok(ApiResponse.success(shortages, "Projected material shortages calculated successfully"));
    }

    @PostMapping("/purchase-orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Create Purchase Order (FR-052)", description = "Records vendor purchase order reference, supplier, amount, and expected delivery date with default PENDING_APPROVAL status.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<PurchaseOrder>> createPurchaseOrder(@Valid @RequestBody PurchaseOrder po) {
        PurchaseOrder created = procurementService.createPurchaseOrder(po);
        return new ResponseEntity<>(ApiResponse.created(created, "Purchase order created successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/purchase-orders/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Update Purchase Order", description = "Updates purchase order details (amount, delivery date, supplier, or items).", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<PurchaseOrder>> updatePurchaseOrder(@PathVariable Long id, @RequestBody PurchaseOrder po) {
        PurchaseOrder updated = procurementService.updatePurchaseOrder(id, po);
        return ResponseEntity.ok(ApiResponse.success(updated, "Purchase order updated successfully"));
    }

    @PutMapping("/purchase-orders/{id}/approval")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER')")
    @Operation(summary = "Process Purchase Order Financial Approval (FR-052)", description = "Financial sign-off by Project Manager / Director for PO (APPROVED, REJECTED, or ISSUED).", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<PurchaseOrder>> updatePurchaseOrderApproval(
            @PathVariable Long id,
            @Valid @RequestBody com.example.BuildTwin._0.domain.procurement.dto.PurchaseOrderApprovalDto approvalDto) {
        PurchaseOrder approved = procurementService.updatePurchaseOrderApproval(id, approvalDto);
        return ResponseEntity.ok(ApiResponse.success(approved, "Purchase order approval status updated successfully"));
    }

    @PutMapping("/purchase-orders/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Update Purchase Order Status", description = "Updates PO lifecycle status (DRAFT, ISSUED, PARTIALLY_FULFILLED, FULFILLED, CANCELLED).", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<PurchaseOrder>> updatePurchaseOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody com.example.BuildTwin._0.domain.procurement.dto.PurchaseOrderStatusDto statusDto) {
        PurchaseOrder updated = procurementService.updatePurchaseOrderStatus(id, statusDto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Purchase order status updated successfully"));
    }

    @GetMapping("/purchase-orders/{id}/fulfillment")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get PO Fulfillment & Delivery Status", description = "Calculates total ordered vs received/accepted quantities across all GRNs and current fulfillment progress.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<com.example.BuildTwin._0.domain.procurement.dto.PoFulfillmentDto>> getPoFulfillmentStatus(@PathVariable Long id) {
        com.example.BuildTwin._0.domain.procurement.dto.PoFulfillmentDto status = procurementService.getPoFulfillmentStatus(id);
        return ResponseEntity.ok(ApiResponse.success(status, "PO fulfillment status fetched successfully"));
    }

    @GetMapping("/purchase-orders/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Purchase Order By ID", description = "Retrieves purchase order details by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<PurchaseOrder>> getPurchaseOrderById(@PathVariable Long id) {
        PurchaseOrder po = procurementService.getPurchaseOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(po, "Purchase order fetched successfully"));
    }

    @DeleteMapping("/purchase-orders/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Cancel Purchase Order", description = "Cancels a purchase order by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deletePurchaseOrder(@PathVariable Long id) {
        procurementService.deletePurchaseOrder(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Purchase order cancelled successfully"));
    }

    @GetMapping({"/purchase-orders", "/pos"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get All Purchase Orders", description = "Retrieves all purchase orders across projects or filtered by optional projectId.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<PurchaseOrder>>> getAllPurchaseOrders(@RequestParam(required = false) Long projectId) {
        List<PurchaseOrder> pos = projectId != null
                ? procurementService.getPurchaseOrdersByProject(projectId)
                : procurementService.getAllPurchaseOrders();
        return ResponseEntity.ok(ApiResponse.success(pos, "Purchase orders fetched successfully"));
    }

    @GetMapping({"/purchase-orders/project/{projectId}", "/pos/project/{projectId}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Purchase Orders By Project", description = "Retrieves all POs associated with a project.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<PurchaseOrder>>> getPurchaseOrdersByProject(@PathVariable Long projectId) {
        List<PurchaseOrder> pos = procurementService.getPurchaseOrdersByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(pos, "Purchase orders fetched successfully"));
    }

    @PostMapping({"/grn", "/goods-receipt-notes"})
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE')")
    @Operation(summary = "Record Goods Receipt Note (GRN) (FR-053)", description = "Records received, accepted, and rejected quantities with delivery evidence photo.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Grn>> createGrn(@Valid @RequestBody Grn grn) {
        Grn created = procurementService.createGrn(grn);
        return new ResponseEntity<>(ApiResponse.created(created, "Goods Receipt Note (GRN) recorded successfully"), HttpStatus.CREATED);
    }

    @GetMapping({"/grn", "/goods-receipt-notes"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get All Goods Receipt Notes", description = "Retrieves all GRNs across projects or filtered by optional projectId.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Grn>>> getAllGrns(@RequestParam(required = false) Long projectId) {
        List<Grn> grns = projectId != null
                ? procurementService.getGrnsByProject(projectId)
                : procurementService.getAllGrns();
        return ResponseEntity.ok(ApiResponse.success(grns, "Goods Receipt Notes fetched successfully"));
    }

    @GetMapping({"/grn/{id}", "/goods-receipt-notes/{id}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get GRN By ID", description = "Retrieves Goods Receipt Note details by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Grn>> getGrnById(@PathVariable Long id) {
        Grn grn = procurementService.getGrnById(id);
        return ResponseEntity.ok(ApiResponse.success(grn, "GRN details fetched successfully"));
    }

    @DeleteMapping({"/grn/{id}", "/goods-receipt-notes/{id}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Void GRN Entry", description = "Voids a GRN record and automatically reverses the stock ledger entry.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteGrn(@PathVariable Long id) {
        procurementService.deleteGrn(id);
        return ResponseEntity.ok(ApiResponse.success(null, "GRN entry voided successfully"));
    }

    @PutMapping({"/grn/{id}", "/goods-receipt-notes/{id}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Update GRN Entry Details", description = "Updates delivery verification metadata such as DC number, vehicle number, delivery evidence photo URL, and remarks.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Grn>> updateGrn(@PathVariable Long id, @RequestBody Grn grn) {
        Grn updated = procurementService.updateGrn(id, grn);
        return ResponseEntity.ok(ApiResponse.success(updated, "GRN details updated successfully"));
    }

    @GetMapping({"/grn/project/{projectId}", "/goods-receipt-notes/project/{projectId}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get GRNs By Project", description = "Retrieves all Goods Receipt Notes for a project.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Grn>>> getGrnsByProject(@PathVariable Long projectId) {
        List<Grn> grns = procurementService.getGrnsByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(grns, "Project GRNs fetched successfully"));
    }

    @GetMapping({"/grn/po/{poId}", "/goods-receipt-notes/po/{poId}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get GRN Log By Purchase Order", description = "Retrieves all GRN delivery receipts for a specific purchase order.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Grn>>> getGrnsByPo(@PathVariable Long poId) {
        List<Grn> grns = procurementService.getGrnsByPo(poId);
        return ResponseEntity.ok(ApiResponse.success(grns, "GRN receipts fetched successfully"));
    }
}
