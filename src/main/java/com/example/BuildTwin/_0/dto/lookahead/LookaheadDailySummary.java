package com.example.BuildTwin._0.dto.lookahead;

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
@Schema(description = "Daily workload distribution breakdown for a single date in the lookahead window")
public class LookaheadDailySummary {

    @Schema(description = "Target calendar date", example = "2026-10-01")
    private LocalDate date;

    @Schema(description = "Day of week", example = "THURSDAY")
    private String dayOfWeek;

    @Schema(description = "Day index in lookahead window (1 to N)", example = "1")
    private Integer dayNumber;

    @Schema(description = "Week index (1 for Days 1-7, 2 for Days 8-14)", example = "1")
    private Integer weekNumber;

    @Schema(description = "Number of active activities scheduled on this day", example = "3")
    private Integer activeActivityCount;

    @Schema(description = "Number of activities commencing on this day", example = "1")
    private Integer startingCount;

    @Schema(description = "Number of activities completing on this day", example = "0")
    private Integer finishingCount;

    @Schema(description = "Activity codes active on this day")
    @Builder.Default
    private List<String> activityCodes = new ArrayList<>();

    @Schema(description = "Activity IDs active on this day")
    @Builder.Default
    private List<Long> activityIds = new ArrayList<>();
}
