package com.example.BuildTwin._0.dto.integration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Deep Cross-Module Activity Integration View")
public class ActivityIntegrationDetailsResponse {

    @Schema(description = "Activity ID", example = "101")
    private Long activityId;

    @Schema(description = "Activity Code", example = "ACT-CIV-001")
    private String activityCode;

    @Schema(description = "Activity Name", example = "Substructure Concrete Works")
    private String activityName;

    @Schema(description = "Engineering Discipline", example = "CIVIL")
    private String discipline;

    @Schema(description = "Status (PLANNED, IN_PROGRESS, COMPLETED, DELAYED, ON_HOLD)", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Planned Start Date", example = "2026-09-01")
    private LocalDate plannedStartDate;

    @Schema(description = "Planned End Date", example = "2026-09-30")
    private LocalDate plannedEndDate;

    @Schema(description = "Actual Start Date", example = "2026-09-03")
    private LocalDate actualStartDate;

    @Schema(description = "Actual End Date")
    private LocalDate actualEndDate;

    @Schema(description = "Planned Duration in Days", example = "30")
    private Integer durationDays;

    @Schema(description = "WBS Weightage Factor", example = "2.5")
    private Double weightage;

    // --- Spatial Hierarchy Links ---
    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Code", example = "PRJ-001")
    private String projectCode;

    @Schema(description = "Work Package ID", example = "10")
    private Long workPackageId;

    @Schema(description = "Work Package Name", example = "Concrete & Masonry")
    private String workPackageName;

    @Schema(description = "Site ID", example = "2")
    private Long siteId;

    @Schema(description = "Site Name", example = "North Tower Site")
    private String siteName;

    @Schema(description = "Building ID", example = "5")
    private Long buildingId;

    @Schema(description = "Building Name", example = "Tower A")
    private String buildingName;

    @Schema(description = "Floor ID", example = "12")
    private Long floorId;

    @Schema(description = "Floor Name", example = "Ground Floor")
    private String floorName;

    @Schema(description = "Zone ID", example = "34")
    private Long zoneId;

    @Schema(description = "Zone Name", example = "West Wing Core")
    private String zoneName;

    // --- Critical Path & Dependencies ---
    @Schema(description = "Total predecessor dependencies count", example = "2")
    private Integer predecessorCount;

    @Schema(description = "Total successor dependencies count", example = "3")
    private Integer successorCount;

    // --- Baseline & EVM Tracking ---
    @Schema(description = "Active baseline version number", example = "1")
    private Integer activeBaselineVersion;

    @Schema(description = "Active baseline planned start date", example = "2026-09-01")
    private LocalDate baselineStartDate;

    @Schema(description = "Active baseline planned end date", example = "2026-09-30")
    private LocalDate baselineEndDate;

    @Schema(description = "Schedule Variance in Days (Actual/Planned finish - Baseline finish)", example = "2")
    private Integer scheduleVarianceDays;

    // --- Physical Quantities & Field Progress ---
    @Schema(description = "Planned Physical Quantity", example = "500.0")
    private Double plannedQuantity;

    @Schema(description = "Completed Physical Quantity", example = "325.0")
    private Double completedQuantity;

    @Schema(description = "Unit of Measure (CUM, SQFT, etc.)", example = "CUM")
    private String uom;

    @Schema(description = "Actual progress percentage", example = "65.0")
    private Double progressPercentage;

    @Schema(description = "Date of last recorded site progress log", example = "2026-10-28")
    private LocalDate lastProgressUpdateDate;

    @Schema(description = "Assigned contractor", example = "L&T Heavy Civil")
    private String assignedContractor;

    @Schema(description = "Module integration status for this activity", example = "FULLY_INTEGRATED")
    private String integrationStatus;
}
