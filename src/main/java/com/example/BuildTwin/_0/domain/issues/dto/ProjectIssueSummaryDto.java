package com.example.BuildTwin._0.domain.issues.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Step 5: Project Issue Analytics & Red Alert Dashboard Summary")
public class ProjectIssueSummaryDto {

    private Long projectId;
    private long totalIssues;

    // Status metrics
    private long openCount;
    private long assignedCount;
    private long inProgressCount;
    private long resolvedCount;
    private long closedCount;

    // Issue Type breakdown
    private long snagCount;
    private long ncrCount;
    private long generalIssueCount;
    private long riskCount;
    private long blockerCount;

    // Severity breakdown
    private long criticalCount;
    private long highCount;
    private long mediumCount;
    private long lowCount;

    // SLA & Overdue metrics
    private long overdueCount;
    private double resolutionPercentage;
}
