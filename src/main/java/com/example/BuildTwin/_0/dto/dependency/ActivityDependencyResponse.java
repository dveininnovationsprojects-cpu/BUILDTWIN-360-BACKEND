package com.example.BuildTwin._0.dto.dependency;

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
@Schema(description = "Response representation of an activity precedence dependency")
public class ActivityDependencyResponse {

    @Schema(description = "Dependency ID", example = "1")
    private Long id;

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name", example = "Ashok Grandeur - Padur, Chennai")
    private String projectName;

    // --- Predecessor Details ---
    @Schema(description = "Predecessor Activity ID", example = "10")
    private Long predecessorId;

    @Schema(description = "Predecessor Activity Code", example = "ACT-CIV-001")
    private String predecessorCode;

    @Schema(description = "Predecessor Activity Name", example = "Column Starter & Rebar Tying")
    private String predecessorName;

    @Schema(description = "Predecessor Activity Discipline", example = "CIVIL")
    private String predecessorDiscipline;

    @Schema(description = "Predecessor Status", example = "COMPLETED")
    private String predecessorStatus;

    @Schema(description = "Predecessor Planned Start Date", example = "2026-09-01")
    private LocalDate predecessorPlannedStartDate;

    @Schema(description = "Predecessor Planned End Date", example = "2026-09-10")
    private LocalDate predecessorPlannedEndDate;

    @Schema(description = "Predecessor Actual Start Date")
    private LocalDate predecessorActualStartDate;

    @Schema(description = "Predecessor Actual End Date")
    private LocalDate predecessorActualEndDate;

    @Schema(description = "Predecessor Progress Percentage", example = "100.0")
    private Double predecessorProgressPercentage;

    @Schema(description = "Predecessor Work Package ID", example = "1")
    private Long predecessorWorkPackageId;

    @Schema(description = "Predecessor Work Package Name", example = "Substructure Works")
    private String predecessorWorkPackageName;

    // --- Successor Details ---
    @Schema(description = "Successor Activity ID", example = "11")
    private Long successorId;

    @Schema(description = "Successor Activity Code", example = "ACT-CIV-002")
    private String successorCode;

    @Schema(description = "Successor Activity Name", example = "Column Concreting & Curing")
    private String successorName;

    @Schema(description = "Successor Activity Discipline", example = "CIVIL")
    private String successorDiscipline;

    @Schema(description = "Successor Status", example = "PLANNED")
    private String successorStatus;

    @Schema(description = "Successor Planned Start Date", example = "2026-09-12")
    private LocalDate successorPlannedStartDate;

    @Schema(description = "Successor Planned End Date", example = "2026-09-20")
    private LocalDate successorPlannedEndDate;

    @Schema(description = "Successor Actual Start Date")
    private LocalDate successorActualStartDate;

    @Schema(description = "Successor Actual End Date")
    private LocalDate successorActualEndDate;

    @Schema(description = "Successor Progress Percentage", example = "0.0")
    private Double successorProgressPercentage;

    @Schema(description = "Successor Work Package ID", example = "1")
    private Long successorWorkPackageId;

    @Schema(description = "Successor Work Package Name", example = "Substructure Works")
    private String successorWorkPackageName;

    // --- Dependency Configuration ---
    @Schema(description = "Dependency Type Code: FS, SS, FF, SF", example = "FS")
    private String dependencyType;

    @Schema(description = "Dependency Type Display Name", example = "Finish-to-Start")
    private String dependencyTypeName;

    @Schema(description = "Lead / Lag time in days", example = "2")
    private Integer lagDays;

    @Schema(description = "Engineering remarks", example = "Curing lag required before stripping formwork")
    private String remarks;

    // --- Schedule & Precedence Impact ---
    @Schema(description = "Flag indicating whether predecessor finish dates conflict with successor start date", example = "false")
    private Boolean hasScheduleConflict;

    @Schema(description = "Conflict severity: NONE, WARNING, CRITICAL", example = "NONE")
    private String conflictSeverity;

    @Schema(description = "Explanation of schedule conflict if any", example = "Predecessor ends after successor planned start")
    private String conflictMessage;

    @Schema(description = "True if predecessor is 100% completed, allowing successor to proceed under FS constraint", example = "true")
    private Boolean isPredecessorSatisfied;

    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Updated timestamp")
    private LocalDateTime updatedAt;
}
