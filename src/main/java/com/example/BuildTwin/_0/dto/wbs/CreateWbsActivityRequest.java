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
@Schema(description = "Request payload for creating a WBS Activity under a Work Package")
public class CreateWbsActivityRequest {

    @NotBlank(message = "Activity code is required")
    @Schema(description = "Unique activity code within work package", example = "ACT-CIV-001")
    private String code;

    @NotBlank(message = "Activity name is required")
    @Schema(description = "Activity name", example = "Column Starter, Rebar Tying & Shuttering")
    private String name;

    @Schema(description = "Trade discipline (CIVIL, STRUCTURAL, MEP, ELECTRICAL, PLUMBING, HVAC, FINISHING)", example = "CIVIL")
    private String discipline;

    @Schema(description = "Detailed scope description of the activity")
    private String description;

    @NotBlank(message = "Unit of Measure (UOM) is required")
    @Schema(description = "Unit of Measure: CUM, SQFT, SQM, RMT, KG, MT, NOS, POINTS", example = "CUM")
    private String uom;

    @NotNull(message = "Planned quantity is required")
    @Positive(message = "Planned quantity must be greater than zero")
    @Schema(description = "Planned quantity of work", example = "450.0")
    private Double plannedQuantity;

    @Schema(description = "Optional physical Site ID", example = "1")
    private Long siteId;

    @Schema(description = "Optional Building ID", example = "1")
    private Long buildingId;

    @Schema(description = "Optional Floor ID", example = "2")
    private Long floorId;

    @Schema(description = "Optional Zone / Unit ID", example = "1")
    private Long zoneId;

    @Schema(description = "Planned start date", example = "2026-09-15")
    private LocalDate plannedStartDate;

    @Schema(description = "Planned end date", example = "2026-10-15")
    private LocalDate plannedEndDate;

    @Pattern(
            regexp = "(?i)^(PLANNED|IN_PROGRESS|COMPLETED|DELAYED|ON_HOLD)?$",
            message = "Invalid status. Allowed: PLANNED, IN_PROGRESS, COMPLETED, DELAYED, ON_HOLD"
    )
    @Schema(description = "Status: PLANNED, IN_PROGRESS, COMPLETED, DELAYED, ON_HOLD", example = "PLANNED")
    private String status;

    @Schema(description = "Assigned contractor name", example = "L&T Construction")
    private String assignedContractor;

    @Schema(description = "Incharge engineer user ID", example = "1")
    private Long inchargeUserId;

    @Schema(description = "Weightage factor for roll-up calculation", example = "1.0")
    private Double weightage;

    @Schema(description = "Execution sequence order", example = "1")
    private Integer sequenceOrder;
}
