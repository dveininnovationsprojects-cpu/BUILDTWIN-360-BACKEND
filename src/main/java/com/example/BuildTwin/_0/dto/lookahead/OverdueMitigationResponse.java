package com.example.BuildTwin._0.dto.lookahead;

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
@Schema(description = "Confirmation response of logged delay cause and catch-up recovery action plan")
public class OverdueMitigationResponse {

    @Schema(description = "Activity ID", example = "101")
    private Long activityId;

    @Schema(description = "Activity Code", example = "ACT-CIV-001")
    private String activityCode;

    @Schema(description = "Activity Name", example = "Floor 1 Column Starter & Rebar Tying")
    private String activityName;

    @Schema(description = "Delay Root Cause Category", example = "MATERIAL_SHORTAGE")
    private String delayReasonCategory;

    @Schema(description = "Delay Root Cause Explanation", example = "Supplier delay in shipping Fe550D rebar bundles due to transport strike")
    private String delayReasonDetails;

    @Schema(description = "Catch-up Action Plan", example = "Local procurement of 15 MT authorized rebar + 2-hour daily overtime shift for steel fixers")
    private String recoveryPlanAction;

    @Schema(description = "Revised Target Completion Date", example = "2026-10-15")
    private LocalDate revisedExpectedCompletionDate;

    @Schema(description = "Additional Labor Allocated", example = "4")
    private Integer additionalLaborAllocated;

    @Schema(description = "Mitigation recorded by user", example = "admin")
    private String recordedBy;

    @Schema(description = "Timestamp when mitigation was logged")
    private LocalDateTime recordedAt;

    @Schema(description = "Operation status message", example = "Overdue activity mitigation and recovery plan logged successfully.")
    private String message;
}
