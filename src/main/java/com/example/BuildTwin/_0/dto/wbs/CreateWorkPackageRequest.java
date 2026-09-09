package com.example.BuildTwin._0.dto.wbs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating a construction Work Package")
public class CreateWorkPackageRequest {

    @Schema(description = "Optional Site ID if specific to a tower or zone", example = "1")
    private Long siteId;

    @NotBlank(message = "Work package code is required")
    @Schema(description = "Unique work package code", example = "WP-CIV-01")
    private String code;

    @NotBlank(message = "Work package name is required")
    @Schema(description = "Work package title", example = "Substructure & RCC Piling Works")
    private String name;

    @NotBlank(message = "Discipline is required")
    @Schema(description = "Discipline / Trade: CIVIL, STRUCTURAL, MEP, ELECTRICAL, PLUMBING, HVAC, FINISHING, FIRE_FIGHTING", example = "CIVIL")
    private String discipline;

    @Schema(description = "Detailed scope description")
    private String description;

    @Pattern(
            regexp = "(?i)^(PLANNED|IN_PROGRESS|ON_HOLD|COMPLETED|CANCELLED)?$",
            message = "Invalid status. Allowed: PLANNED, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED"
    )
    @Schema(description = "Status: PLANNED, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Planned start date", example = "2026-09-01")
    private LocalDate plannedStartDate;

    @Schema(description = "Planned end date", example = "2026-11-30")
    private LocalDate plannedEndDate;

    @Schema(description = "Actual start date", example = "2026-09-05")
    private LocalDate actualStartDate;

    @Schema(description = "Budget allocation for package", example = "5000000.00")
    private BigDecimal budgetAmount;

    @Schema(description = "Contractor or Subcontractor firm", example = "L&T GeoStructure")
    private String assignedContractor;

    @Schema(description = "Incharge engineer or supervisor user ID", example = "3")
    private Long inchargeUserId;
}
