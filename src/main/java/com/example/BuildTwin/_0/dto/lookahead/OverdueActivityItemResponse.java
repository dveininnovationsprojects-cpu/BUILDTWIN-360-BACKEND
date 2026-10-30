package com.example.BuildTwin._0.dto.lookahead;

import com.example.BuildTwin._0.model.OverdueAgingBucket;
import com.example.BuildTwin._0.model.OverdueSeverity;
import com.example.BuildTwin._0.model.OverdueType;
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
@Schema(description = "Detailed overdue construction activity report card with delay analytics, downstream impact, and constraint blockers")
public class OverdueActivityItemResponse {

    @Schema(description = "WBS Activity ID", example = "101")
    private Long activityId;

    @Schema(description = "Activity Code", example = "ACT-CIV-001")
    private String code;

    @Schema(description = "Activity Name", example = "Floor 1 Column Starter & Rebar Tying")
    private String name;

    @Schema(description = "Discipline / Trade", example = "CIVIL")
    private String discipline;

    @Schema(description = "WBS Level (1=Summary/Parent, 2=Task, 3=Subtask)", example = "2")
    private Integer level;

    @Schema(description = "WBS Path", example = "/1/2")
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

    @Schema(description = "Building Name", example = "Tower A - Premium Suites")
    private String buildingName;

    @Schema(description = "Floor Name", example = "First Typical Floor")
    private String floorName;

    @Schema(description = "Zone Name", example = "3BHK Luxury Flat 101")
    private String zoneName;

    @Schema(description = "Activity Status", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Original Planned Start Date", example = "2026-09-10")
    private LocalDate plannedStartDate;

    @Schema(description = "Original Planned End Date", example = "2026-09-25")
    private LocalDate plannedEndDate;

    @Schema(description = "Actual Start Date", example = "2026-09-12")
    private LocalDate actualStartDate;

    @Schema(description = "Actual End Date")
    private LocalDate actualEndDate;

    @Schema(description = "Planned Duration in Days", example = "15")
    private Integer plannedDurationDays;

    @Schema(description = "Overdue Category (OVERDUE_FINISH, OVERDUE_START, BEHIND_SCHEDULE)", example = "OVERDUE_FINISH")
    private OverdueType overdueType;

    @Schema(description = "Number of days delayed past planned deadline", example = "10")
    private Long daysOverdue;

    @Schema(description = "Delay Aging Classification (DAYS_1_TO_7, DAYS_8_TO_14, DAYS_15_TO_30, OVER_30_DAYS)", example = "DAYS_8_TO_14")
    private OverdueAgingBucket agingBucket;

    @Schema(description = "Delay Severity (CRITICAL, HIGH, MEDIUM, LOW)", example = "HIGH")
    private OverdueSeverity severity;

    @Schema(description = "Total Planned Physical Quantity", example = "25.0")
    private Double plannedQuantity;

    @Schema(description = "Completed Physical Quantity", example = "15.0")
    private Double completedQuantity;

    @Schema(description = "Remaining Physical Work Quantity", example = "10.0")
    private Double remainingQuantity;

    @Schema(description = "Unit of Measure", example = "MT")
    private String uom;

    @Schema(description = "Current Progress Percentage", example = "60.0")
    private Double progressPercentage;

    @Schema(description = "Expected Progress Percentage by reference date", example = "100.0")
    private Double expectedProgressPercentage;

    @Schema(description = "Progress Slippage Percentage (Expected - Actual)", example = "40.0")
    private Double progressSlippagePercentage;

    @Schema(description = "Assigned Contractor", example = "L&T Construction (Civil Div)")
    private String assignedContractor;

    @Schema(description = "Incharge User ID", example = "1")
    private Long inchargeUserId;

    @Schema(description = "Count of downstream / successor activities blocked by this overdue activity", example = "2")
    private Integer impactedSuccessorCount;

    @Schema(description = "Codes and names of blocked downstream activities")
    @Builder.Default
    private List<String> impactedSuccessors = new ArrayList<>();

    @Schema(description = "Whether this activity has a lookahead commitment logged", example = "true")
    private Boolean hasCommitment;

    @Schema(description = "Commitment Status", example = "BLOCKED")
    private String commitmentStatus;

    @Schema(description = "Active field constraint blockers (Materials, Drawings, Machinery, Permits, etc.)")
    @Builder.Default
    private List<String> constraintBlockers = new ArrayList<>();

    @Schema(description = "Latest reported blocker reason or variance notes", example = "Delayed delivery of Fe550D rebar from stockyard")
    private String latestBlockerReason;

    @Schema(description = "Actionable recovery / mitigation recommendation", example = "High priority: Mobilize additional rebar crew to clear 10-day delay and unblock 2 downstream tasks.")
    private String recommendedRecoveryAction;
}
