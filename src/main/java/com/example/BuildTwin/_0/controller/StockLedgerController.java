package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.materials.dto.StockTransactionDto;
import com.example.BuildTwin._0.domain.materials.model.StockLedger;
import com.example.BuildTwin._0.domain.materials.service.StockLedgerService;
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
@RequestMapping("/api/v1/stock-ledger")
@RequiredArgsConstructor
@Tag(name = "6. Stock Ledger Architecture", description = "Immutable append-only stock ledger audit trail & ACID inventory locks (FR-054, FR-055, FR-056, FR-057)")
public class StockLedgerController {

    private final StockLedgerService stockLedgerService;

    @PostMapping("/transaction")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Record Stock Transaction (RECEIPT, ISSUE, CONSUMPTION, RETURN, ADJUSTMENT)", description = "Inserts an immutable ledger audit trail record and atomically updates stock balances with strict negative-stock locks.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<StockLedger>> recordStockTransaction(@Valid @RequestBody StockTransactionDto request) {
        StockLedger entry = stockLedgerService.recordTransaction(request);
        return new ResponseEntity<>(ApiResponse.created(entry, "Stock ledger transaction recorded successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/issue")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Issue Material From Store (FR-054)", description = "Issues material from store to specific WBS Activity, Zone, or Contractor.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<StockLedger>> issueMaterial(@Valid @RequestBody StockTransactionDto request) {
        StockLedger entry = stockLedgerService.issueMaterial(request);
        return new ResponseEntity<>(ApiResponse.created(entry, "Material issued successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/consumption")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Record Material Consumption (FR-055)", description = "Records actual material consumption on site work against WBS activity and Zone.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<StockLedger>> recordConsumption(@Valid @RequestBody StockTransactionDto request) {
        StockLedger entry = stockLedgerService.recordConsumption(request);
        return new ResponseEntity<>(ApiResponse.created(entry, "Material consumption recorded successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/wastage")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PROCUREMENT_STORE')")
    @Operation(summary = "Record Material Wastage (FR-055)", description = "Tracks material wastage on site and updates stock balance transactionally.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<StockLedger>> recordWastage(@Valid @RequestBody StockTransactionDto request) {
        StockLedger entry = stockLedgerService.recordWastage(request);
        return new ResponseEntity<>(ApiResponse.created(entry, "Material wastage recorded successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/reconcile")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'PROCUREMENT_STORE', 'AUDITOR')")
    @Operation(summary = "Perform Stock Reconciliation (FR-057)", description = "Compares system stock vs physical stock count, computes variance, and logs stock adjustment.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<com.example.BuildTwin._0.domain.materials.dto.StockReconciliationResultDto>> reconcileStock(
            @Valid @RequestBody com.example.BuildTwin._0.domain.materials.dto.StockReconciliationDto request) {
        com.example.BuildTwin._0.domain.materials.dto.StockReconciliationResultDto result = stockLedgerService.reconcileStock(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Stock reconciliation completed successfully"));
    }

    @GetMapping("/material/{materialId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Audit Trail By Material", description = "Retrieves complete immutable stock audit log for a specific material.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<StockLedger>>> getLedgerByMaterial(@PathVariable Long materialId) {
        List<StockLedger> entries = stockLedgerService.getLedgerEntriesByMaterial(materialId);
        return ResponseEntity.ok(ApiResponse.success(entries, "Stock ledger audit entries fetched successfully"));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Audit Trail By Project", description = "Retrieves complete immutable stock audit log for a specific project.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<StockLedger>>> getLedgerByProject(@PathVariable Long projectId) {
        List<StockLedger> entries = stockLedgerService.getLedgerEntriesByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(entries, "Project stock ledger audit entries fetched successfully"));
    }

    @GetMapping("/activity/{activityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'SITE_SUPERVISOR', 'PROCUREMENT_STORE', 'QUANTITY_COST_COORDINATOR', 'QUALITY_ENGINEER', 'DATA_ANALYST', 'AUDITOR')")
    @Operation(summary = "Get Audit Trail By WBS Activity", description = "Retrieves stock issue & consumption audit logs linked to a specific WBS activity.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<StockLedger>>> getLedgerByActivity(@PathVariable Long activityId) {
        List<StockLedger> entries = stockLedgerService.getLedgerEntriesByActivity(activityId);
        return ResponseEntity.ok(ApiResponse.success(entries, "Activity stock ledger audit entries fetched successfully"));
    }
}
