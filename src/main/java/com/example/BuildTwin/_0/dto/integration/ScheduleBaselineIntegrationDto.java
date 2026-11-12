package com.example.BuildTwin._0.dto.integration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Schedule Baseline & Revision Module Integration Details")
public class ScheduleBaselineIntegrationDto {

    @Schema(description = "Active baseline version number", example = "1")
    private Integer activeBaselineVersion;

    @Schema(description = "Active baseline name", example = "Master Contract Baseline v1.0")
    private String activeBaselineName;

    @Schema(description = "Whether the active baseline has executive approval", example = "true")
    private Boolean isBaselineApproved;

    @Schema(description = "Total schedule revisions recorded", example = "3")
    private Long scheduleRevisionsCount;

    @Schema(description = "Total schedule delay alerts triggered", example = "2")
    private Long delayAlertsCount;

    @Schema(description = "Critical path activity dependencies count", example = "34")
    private Long dependencyCount;

    @Schema(description = "Schedule Baseline module status", example = "FROZEN_ACTIVE")
    private String status;
}
