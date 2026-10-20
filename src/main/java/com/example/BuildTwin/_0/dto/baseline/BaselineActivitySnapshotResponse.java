package com.example.BuildTwin._0.dto.baseline;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Individual activity snapshot stored inside a frozen baseline")
public class BaselineActivitySnapshotResponse {

    @Schema(description = "Snapshot ID", example = "1")
    private Long id;

    @Schema(description = "Baseline ID", example = "1")
    private Long baselineId;

    @Schema(description = "Activity ID", example = "10")
    private Long activityId;

    @Schema(description = "Activity Code", example = "ACT-CIV-001")
    private String activityCode;

    @Schema(description = "Activity Name", example = "Site Excavation & Earthwork")
    private String activityName;

    @Schema(description = "Work Package ID", example = "1")
    private Long workPackageId;

    @Schema(description = "Work Package Name", example = "Substructure Works")
    private String workPackageName;

    @Schema(description = "Discipline", example = "CIVIL")
    private String discipline;

    @Schema(description = "UOM", example = "CUM")
    private String uom;

    @Schema(description = "Frozen Planned Quantity", example = "500.0")
    private Double plannedQuantity;

    @Schema(description = "Frozen Planned Start Date", example = "2026-04-01")
    private LocalDate plannedStartDate;

    @Schema(description = "Frozen Planned End Date", example = "2026-04-10")
    private LocalDate plannedEndDate;

    @Schema(description = "Frozen Duration in Days", example = "9")
    private Integer plannedDurationDays;

    @Schema(description = "Weightage", example = "1.0")
    private Double weightage;

    @Schema(description = "Contractor Assigned at Freeze Time", example = "L&T GeoStructure")
    private String assignedContractor;
}
