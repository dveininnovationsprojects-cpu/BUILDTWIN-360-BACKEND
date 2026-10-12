package com.example.BuildTwin._0.domain.quality.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Quality, NCR & Snag KPI metrics summary for project")
public class QualitySummaryDto {

    @Schema(description = "Project ID", example = "100")
    private Long projectId;

    @Schema(description = "Total quality records", example = "24")
    private long totalIssues;

    @Schema(description = "Open Snags count", example = "8")
    private long openSnags;

    @Schema(description = "Open Non-Conformance Reports (NCR) count", example = "3")
    private long openNcrs;

    @Schema(description = "Issues awaiting verification/closure", example = "4")
    private long pendingVerification;

    @Schema(description = "Verified & closed issues count", example = "13")
    private long closedCount;

    @Schema(description = "Critical severity defect count", example = "2")
    private long criticalCount;

    @Schema(description = "High severity defect count", example = "5")
    private long highCount;
}
