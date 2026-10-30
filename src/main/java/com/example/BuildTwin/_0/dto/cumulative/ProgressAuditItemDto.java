package com.example.BuildTwin._0.dto.cumulative;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Single audit discrepancy detected between stored activity progress and verified progress history logs")
public class ProgressAuditItemDto {

    @Schema(description = "Activity ID", example = "15")
    private Long activityId;

    @Schema(description = "Activity Code", example = "ACT-CIV-001")
    private String activityCode;

    @Schema(description = "Activity Name", example = "Substructure Concrete Works")
    private String activityName;

    @Schema(description = "Work Package Code", example = "WP-CIV-01")
    private String workPackageCode;

    @Schema(description = "Stored completed quantity on activity entity", example = "200.0")
    private Double storedCompletedQuantity;

    @Schema(description = "Verified completed quantity from progress logs", example = "220.0")
    private Double verifiedCompletedQuantityFromLogs;

    @Schema(description = "Stored progress percentage on activity entity", example = "40.0")
    private Double storedProgressPercentage;

    @Schema(description = "Calculated progress percentage based on verified logs and planned quantity", example = "44.0")
    private Double calculatedProgressPercentage;

    @Schema(description = "Quantity delta (Verified - Stored)", example = "20.0")
    private Double quantityDelta;

    @Schema(description = "Percentage delta (Calculated - Stored)", example = "4.0")
    private Double percentageDelta;

    @Schema(description = "Type of discrepancy: QUANTITY_MISMATCH, PERCENTAGE_MISMATCH, OVER_EXECUTED, ORPHANED_LOGS, CLEAN", example = "QUANTITY_MISMATCH")
    private String discrepancyType;

    @Schema(description = "Detailed audit explanation or recommended action", example = "Stored quantity lags verified journal logs by 20.0 CUM. Reconciliation recommended.")
    private String description;

    @Schema(description = "Severity of discrepancy: LOW, MEDIUM, HIGH, CRITICAL", example = "MEDIUM")
    private String severity;
}
