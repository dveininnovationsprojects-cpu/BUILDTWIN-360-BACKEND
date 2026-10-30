package com.example.BuildTwin._0.dto.cumulative;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Result of cumulative progress auto-reconciliation and data healing operation")
public class ProgressReconciliationResponse {

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name", example = "Skyline Residency Phase 1")
    private String projectName;

    @Schema(description = "Reconciliation timestamp")
    private LocalDateTime reconciliationTimestamp;

    @Schema(description = "Total activities examined during reconciliation", example = "45")
    private Integer totalActivitiesScanned;

    @Schema(description = "Total activities whose cumulative quantities or progress percentages were healed/repaired", example = "3")
    private Integer activitiesRepairedCount;

    @Schema(description = "Total parent tasks whose rolled-up percentages were updated", example = "2")
    private Integer parentTasksUpdatedCount;

    @Schema(description = "List of repaired activity codes", example = "[\"ACT-CIV-001\", \"ACT-CIV-004\"]")
    @Builder.Default
    private List<String> repairedActivityCodes = new ArrayList<>();

    @Schema(description = "User or administrator who initiated reconciliation", example = "admin")
    private String reconciledBy;

    @Schema(description = "Status message", example = "Cumulative progress successfully reconciled and synchronized with verified progress history logs.")
    private String message;
}
