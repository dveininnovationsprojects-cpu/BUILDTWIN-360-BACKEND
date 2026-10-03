package com.example.BuildTwin._0.dto.lookahead;

import com.example.BuildTwin._0.model.LookaheadCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed activity item within the 7-Day Look-Ahead Schedule")
public class LookaheadActivityItemResponse {

    @Schema(description = "WBS Activity ID", example = "1")
    private Long activityId;

    @Schema(description = "Activity Code", example = "ACT-CIV-001")
    private String code;

    @Schema(description = "Activity Name", example = "Floor 1 Column Starter & Rebar Tying")
    private String name;

    @Schema(description = "Discipline / Trade", example = "CIVIL")
    private String discipline;

    @Schema(description = "WBS Level", example = "2")
    private Integer level;

    @Schema(description = "WBS Hierarchy Path", example = "/1/2")
    private String wbsPath;

    @Schema(description = "Work Package ID", example = "1")
    private Long workPackageId;

    @Schema(description = "Work Package Code", example = "WP-CIV-01")
    private String workPackageCode;

    @Schema(description = "Work Package Name", example = "Substructure & RCC Framing Works")
    private String workPackageName;

    @Schema(description = "Site ID", example = "1")
    private Long siteId;

    @Schema(description = "Site Name", example = "Tower A (Stilt + 18 Floors)")
    private String siteName;

    @Schema(description = "Building ID", example = "1")
    private Long buildingId;

    @Schema(description = "Building Name", example = "Tower A - Royal Suites")
    private String buildingName;

    @Schema(description = "Floor ID", example = "1")
    private Long floorId;

    @Schema(description = "Floor Name", example = "First Typical Floor")
    private String floorName;

    @Schema(description = "Zone ID", example = "1")
    private Long zoneId;

    @Schema(description = "Zone Name", example = "3BHK Luxury Flat 101")
    private String zoneName;

    @Schema(description = "Activity Current Status", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Planned Start Date", example = "2026-09-26")
    private LocalDate plannedStartDate;

    @Schema(description = "Planned End Date", example = "2026-10-20")
    private LocalDate plannedEndDate;

    @Schema(description = "Actual Start Date", example = "2026-09-28")
    private LocalDate actualStartDate;

    @Schema(description = "Actual End Date")
    private LocalDate actualEndDate;

    @Schema(description = "Planned Duration in Days", example = "25")
    private Integer durationDays;

    @Schema(description = "Total Planned Quantity", example = "240.0")
    private Double plannedQuantity;

    @Schema(description = "Current Completed Quantity", example = "120.0")
    private Double completedQuantity;

    @Schema(description = "Unit of Measure", example = "CUM")
    private String uom;

    @Schema(description = "Progress Percentage", example = "50.0")
    private Double progressPercentage;

    @Schema(description = "Assigned Contractor", example = "L&T Construction (Civil Div)")
    private String assignedContractor;

    @Schema(description = "Incharge User ID")
    private Long inchargeUserId;

    @Schema(description = "Lookahead Classification in this window", example = "ONGOING")
    private LookaheadCategory category;

    @Schema(description = "Days Overdue (if planned end date passed and incomplete)", example = "0")
    private Long daysOverdue;

    @Schema(description = "Dates within the 7-day window where this activity is scheduled to work")
    @Builder.Default
    private List<LocalDate> activeOnDays = new ArrayList<>();

    @Schema(description = "Predecessor Readiness Status (READY, PENDING_DEPENDENCIES, NO_PREDECESSORS)", example = "READY")
    private String predecessorStatus;

    @Schema(description = "Count of incomplete predecessor activities", example = "0")
    private Integer unresolvedPredecessorCount;

    @Schema(description = "List of unresolved predecessor activity codes")
    @Builder.Default
    private List<String> unresolvedPredecessors = new ArrayList<>();

    @Schema(description = "Whether this activity has been committed into the weekly plan", example = "true")
    private Boolean isCommitted;

    @Schema(description = "Weekly work plan commitment details (if committed)")
    private LookaheadCommitmentResponse commitment;

    @Schema(description = "Readiness Classification (READY_FOR_EXECUTION, CONSTRAINED, BLOCKED, COMPLETED)", example = "READY_FOR_EXECUTION")
    private String readinessStatus;
}
