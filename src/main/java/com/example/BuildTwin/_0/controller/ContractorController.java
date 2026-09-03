package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.domain.labour.dto.ContractorPerformanceSummaryDto;
import com.example.BuildTwin._0.domain.labour.dto.ContractorRequestDto;
import com.example.BuildTwin._0.domain.labour.dto.TradeDto;
import com.example.BuildTwin._0.domain.labour.enums.TradeCategory;
import com.example.BuildTwin._0.domain.labour.model.Contractor;
import com.example.BuildTwin._0.domain.labour.service.ContractorService;
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
@RequestMapping("/api/v1/contractors")
@RequiredArgsConstructor
@Tag(name = "2. Contractor Master", description = "Subcontractor management, business identity & trade specialization (FR-040)")
public class ContractorController {

    private final ContractorService contractorService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    @Operation(summary = "Create Contractor Profile (FR-040)", description = "Maintains verified contractor/subcontractor records with business identity, trade specialization, contact details, and status.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Contractor>> createContractor(@Valid @RequestBody ContractorRequestDto request) {
        Contractor contractor = contractorService.createContractor(request);
        return new ResponseEntity<>(ApiResponse.created(contractor, "Contractor profile created successfully"), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'STORE_KEEPER', 'EXECUTIVE')")
    @Operation(summary = "Get All Contractors", description = "Retrieves directory of all contractors.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Contractor>>> getAllContractors() {
        List<Contractor> contractors = contractorService.getAllContractors();
        return ResponseEntity.ok(ApiResponse.success(contractors, "Contractors fetched successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'STORE_KEEPER', 'EXECUTIVE')")
    @Operation(summary = "Get Contractor By ID", description = "Retrieves contractor details by ID.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Contractor>> getContractorById(@PathVariable Long id) {
        Contractor contractor = contractorService.getContractorById(id);
        return ResponseEntity.ok(ApiResponse.success(contractor, "Contractor details fetched successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    @Operation(summary = "Update Contractor Profile", description = "Updates contractor/subcontractor information and status.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Contractor>> updateContractor(@PathVariable Long id, @Valid @RequestBody ContractorRequestDto request) {
        Contractor updated = contractorService.updateContractor(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Contractor updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    @Operation(summary = "Deactivate Contractor Profile", description = "Sets contractor status to INACTIVE.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteContractor(@PathVariable Long id) {
        contractorService.deleteContractor(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Contractor deactivated successfully"));
    }

    @GetMapping("/subcontractors")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'EXECUTIVE')")
    @Operation(summary = "List Subcontractors", description = "Retrieves list of all registered subcontractors.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Contractor>>> getSubcontractors() {
        List<Contractor> subcontractors = contractorService.getSubcontractors();
        return ResponseEntity.ok(ApiResponse.success(subcontractors, "Subcontractors fetched successfully"));
    }

    @GetMapping("/{id}/subcontractors")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'EXECUTIVE')")
    @Operation(summary = "Get Subcontractors By Parent Contractor", description = "Retrieves subcontractors associated with a specific main contractor.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Contractor>>> getSubcontractorsByParent(@PathVariable Long id) {
        List<Contractor> subcontractors = contractorService.getSubcontractorsByParent(id);
        return ResponseEntity.ok(ApiResponse.success(subcontractors, "Subcontractors fetched by parent successfully"));
    }

    @GetMapping("/trades")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'STORE_KEEPER', 'EXECUTIVE')")
    @Operation(summary = "Get Available Trade Categories", description = "Retrieves list of all supported trade specializations and metadata.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<TradeDto>>> getTradeCategories() {
        List<TradeDto> trades = contractorService.getTradeCategories();
        return ResponseEntity.ok(ApiResponse.success(trades, "Trade categories fetched successfully"));
    }

    @GetMapping("/trade/{trade}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'STORE_KEEPER', 'EXECUTIVE')")
    @Operation(summary = "Get Contractors By Trade", description = "Filter contractors by trade specialization.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<List<Contractor>>> getContractorsByTrade(@PathVariable TradeCategory trade) {
        List<Contractor> contractors = contractorService.getContractorsByTrade(trade);
        return ResponseEntity.ok(ApiResponse.success(contractors, "Contractors filtered by trade successfully"));
    }

    @GetMapping("/{id}/performance-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER', 'PLANNING_ENGINEER', 'EXECUTIVE')")
    @Operation(summary = "Get Contractor Performance Summary API", description = "Provides comprehensive progress, quality, delay, and productivity metrics for a contractor.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<ContractorPerformanceSummaryDto>> getContractorPerformanceSummary(@PathVariable Long id) {
        ContractorPerformanceSummaryDto summary = contractorService.getContractorPerformanceSummary(id);
        return ResponseEntity.ok(ApiResponse.success(summary, "Contractor performance summary fetched successfully"));
    }
}
