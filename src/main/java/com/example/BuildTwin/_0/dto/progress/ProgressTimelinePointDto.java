package com.example.BuildTwin._0.dto.progress;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Single timeline data point representing daily output, cumulative progress %, and planned curve benchmark")
public class ProgressTimelinePointDto {

    @Schema(description = "Date of progress", example = "2026-09-28")
    private LocalDate date;

    @Schema(description = "Physical quantity completed on this date", example = "45.0")
    private Double quantityToday;

    @Schema(description = "Cumulative quantity completed up to this date", example = "220.0")
    private Double cumulativeQuantity;

    @Schema(description = "Cumulative actual progress percentage achieved", example = "44.0")
    private Double cumulativePercentage;

    @Schema(description = "Target / Planned cumulative progress percentage based on S-curve timeline", example = "40.0")
    private Double plannedCumulativePercentage;

    @Schema(description = "Manpower count deployed on date", example = "12")
    private Integer manpower;

    @Schema(description = "Whether a hindrance/blocker occurred on this date", example = "false")
    private Boolean isBlocker;
}
