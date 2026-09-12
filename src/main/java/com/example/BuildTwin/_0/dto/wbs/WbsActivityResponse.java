package com.example.BuildTwin._0.dto.wbs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response representation of a construction WBS Activity / Task")
public class WbsActivityResponse {

    @Schema(description = "Activity ID", example = "1")
    private Long id;

    @Schema(description = "Work Package ID", example = "1")
    private Long workPackageId;

    @Schema(description = "Work Package Code", example = "WP-CIV-01")
    private String workPackageCode;

    @Schema(description = "Work Package Name", example = "Substructure & RCC Framing Works")
    private String workPackageName;

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name", example = "Ashok Grandeur - Padur, Chennai")
    private String projectName;

    @Schema(description = "Site ID", example = "1")
    private Long siteId;

    @Schema(description = "Site Name", example = "Tower A (Stilt + 18 Floors)")
    private String siteName;

    @Schema(description = "Building ID", example = "1")
    private Long buildingId;

    @Schema(description = "Building Name", example = "Tower A - Premium Suites")
    private String buildingName;

    @Schema(description = "Floor ID", example = "2")
    private Long floorId;

    @Schema(description = "Floor Name", example = "First Typical Floor")
    private String floorName;

    @Schema(description = "Zone ID", example = "1")
    private Long zoneId;

    @Schema(description = "Zone Name", example = "3BHK Luxury Flat 101")
    private String zoneName;

    @Schema(description = "Activity Code", example = "ACT-CIV-001")
    private String code;

    @Schema(description = "Activity Name", example = "Column Starter, Rebar Tying & Shuttering")
    private String name;

    @Schema(description = "Trade Discipline", example = "CIVIL")
    private String discipline;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Unit of Measure", example = "CUM")
    private String uom;

    @Schema(description = "Planned quantity", example = "450.0")
    private Double plannedQuantity;

    @Schema(description = "Completed quantity", example = "150.0")
    private Double completedQuantity;

    @Schema(description = "Progress percentage", example = "33.33")
    private Double progressPercentage;

    @Schema(description = "Planned start date", example = "2026-09-15")
    private LocalDate plannedStartDate;

    @Schema(description = "Planned end date", example = "2026-10-15")
    private LocalDate plannedEndDate;

    @Schema(description = "Actual start date", example = "2026-09-18")
    private LocalDate actualStartDate;

    @Schema(description = "Actual end date")
    private LocalDate actualEndDate;

    @Schema(description = "Status: PLANNED, IN_PROGRESS, COMPLETED, DELAYED, ON_HOLD", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Assigned contractor", example = "L&T Construction")
    private String assignedContractor;

    @Schema(description = "Incharge engineer user ID", example = "1")
    private Long inchargeUserId;

    @Schema(description = "Incharge engineer user name", example = "admin")
    private String inchargeUserName;

    @Schema(description = "Weightage factor", example = "1.0")
    private Double weightage;

    @Schema(description = "Sequence order", example = "1")
    private Integer sequenceOrder;

    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Updated timestamp")
    private LocalDateTime updatedAt;
}
