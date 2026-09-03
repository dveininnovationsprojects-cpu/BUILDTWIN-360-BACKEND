package com.example.BuildTwin._0.dto.wbs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response representation of a construction Work Package (WBS)")
public class WorkPackageResponse {

    @Schema(description = "Work Package ID", example = "1")
    private Long id;

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name", example = "Ashok Grandeur")
    private String projectName;

    @Schema(description = "Site ID", example = "1")
    private Long siteId;

    @Schema(description = "Site Name", example = "Tower A")
    private String siteName;

    @Schema(description = "Work package code", example = "WP-CIV-01")
    private String code;

    @Schema(description = "Work package title", example = "Substructure & RCC Piling Works")
    private String name;

    @Schema(description = "Discipline / Trade (CIVIL, STRUCTURAL, MEP, ELECTRICAL, PLUMBING, HVAC, FINISHING, FIRE_FIGHTING)", example = "CIVIL")
    private String discipline;

    @Schema(description = "Detailed scope description")
    private String description;

    @Schema(description = "Execution status (PLANNED, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED)", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Planned start date", example = "2026-09-01")
    private LocalDate plannedStartDate;

    @Schema(description = "Planned end date", example = "2026-11-30")
    private LocalDate plannedEndDate;

    @Schema(description = "Actual start date", example = "2026-09-05")
    private LocalDate actualStartDate;

    @Schema(description = "Actual end date")
    private LocalDate actualEndDate;

    @Schema(description = "Budget allocation for package", example = "5000000.00")
    private BigDecimal budgetAmount;

    @Schema(description = "Contractor or Subcontractor firm", example = "L&T GeoStructure")
    private String assignedContractor;

    @Schema(description = "Incharge engineer or user ID", example = "3")
    private Long inchargeUserId;

    @Schema(description = "Incharge engineer name", example = "ramesh_site")
    private String inchargeUserName;

    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Updated timestamp")
    private LocalDateTime updatedAt;
}
