package com.example.BuildTwin._0.dto.dpr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response representation of a DPR WBS activity progress line item")
public class DprActivityItemResponse {

    @Schema(description = "Item ID", example = "1")
    private Long id;

    @Schema(description = "Activity ID", example = "101")
    private Long activityId;

    @Schema(description = "WBS Activity Code", example = "ACT-CIV-001")
    private String activityCode;

    @Schema(description = "WBS Activity Name", example = "Substructure Concrete Works")
    private String activityName;

    @Schema(description = "Discipline category", example = "CIVIL")
    private String discipline;

    @Schema(description = "Unit of Measure", example = "CUM")
    private String uom;

    @Schema(description = "Total planned baseline quantity", example = "500.0")
    private Double plannedQuantity;

    @Schema(description = "Quantity completed today", example = "45.5")
    private Double quantityToday;

    @Schema(description = "Cumulative quantity completed up to this date", example = "245.5")
    private Double cumulativeQuantity;

    @Schema(description = "Incremental percentage progress achieved today", example = "9.1")
    private Double progressPercentageToday;

    @Schema(description = "Cumulative completion percentage", example = "49.1")
    private Double cumulativeProgressPercentage;

    @Schema(description = "Location reference / Grid line", example = "Tower A - Level 3 - Pour B")
    private String locationReference;

    @Schema(description = "Remarks", example = "Concrete pour completed smoothly")
    private String remarks;
}
