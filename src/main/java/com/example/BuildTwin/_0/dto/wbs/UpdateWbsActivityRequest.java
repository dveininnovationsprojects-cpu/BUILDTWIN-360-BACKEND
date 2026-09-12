package com.example.BuildTwin._0.dto.wbs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for updating a WBS Activity")
public class UpdateWbsActivityRequest {

    @NotBlank(message = "Activity code is required")
    @Schema(description = "Activity code", example = "ACT-CIV-001")
    private String code;

    @NotBlank(message = "Activity name is required")
    @Schema(description = "Activity name", example = "Column Starter, Rebar Tying & Shuttering")
    private String name;

    @Schema(description = "Trade discipline", example = "CIVIL")
    private String discipline;

    @Schema(description = "Detailed description")
    private String description;

    @NotBlank(message = "UOM is required")
    @Schema(description = "Unit of Measure", example = "CUM")
    private String uom;

    @NotNull(message = "Planned quantity is required")
    @Positive(message = "Planned quantity must be greater than zero")
    @Schema(description = "Planned quantity", example = "450.0")
    private Double plannedQuantity;

    @Schema(description = "Completed quantity", example = "150.0")
    private Double completedQuantity;

    @Schema(description = "Progress percentage (0 - 100)", example = "33.33")
    private Double progressPercentage;

    @Schema(description = "Site ID", example = "1")
    private Long siteId;

    @Schema(description = "Building ID", example = "1")
    private Long buildingId;

    @Schema(description = "Floor ID", example = "2")
    private Long floorId;

    @Schema(description = "Zone ID", example = "1")
    private Long zoneId;

    @Schema(description = "Planned start date", example = "2026-09-15")
    private LocalDate plannedStartDate;

    @Schema(description = "Planned end date", example = "2026-10-15")
    private LocalDate plannedEndDate;

    @Schema(description = "Actual start date", example = "2026-09-18")
    private LocalDate actualStartDate;

    @Schema(description = "Actual end date")
    private LocalDate actualEndDate;

    @Pattern(
            regexp = "(?i)^(PLANNED|IN_PROGRESS|COMPLETED|DELAYED|ON_HOLD)?$",
            message = "Invalid status. Allowed: PLANNED, IN_PROGRESS, COMPLETED, DELAYED, ON_HOLD"
    )
    @Schema(description = "Status: PLANNED, IN_PROGRESS, COMPLETED, DELAYED, ON_HOLD", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Assigned contractor", example = "L&T Construction")
    private String assignedContractor;

    @Schema(description = "Incharge user ID", example = "1")
    private Long inchargeUserId;

    @Schema(description = "Weightage factor", example = "1.0")
    private Double weightage;

    @Schema(description = "Execution sequence order", example = "1")
    private Integer sequenceOrder;
}
