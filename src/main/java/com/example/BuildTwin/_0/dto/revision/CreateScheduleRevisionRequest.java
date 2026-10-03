package com.example.BuildTwin._0.dto.revision;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to initiate and create a draft schedule revision package for a project")
public class CreateScheduleRevisionRequest {

    @Schema(description = "Revision code (Optional, auto-generated e.g. REV-01 if omitted)", example = "REV-01")
    private String revisionCode;

    @NotBlank(message = "Revision name is required")
    @Schema(description = "Name or title for this schedule revision", example = "Baseline Revision 1 (Monsoon Delay & Scope Addition)")
    private String revisionName;

    @Schema(description = "Revision proposal date (defaults to current date if null)", example = "2026-09-30")
    private LocalDate revisionDate;

    @Schema(description = "Comprehensive justification/narrative for schedule change", example = "Severe monsoon downpours caused 12 days stoppage; foundation redesigned per PMC instruction.")
    private String reasonForRevision;

    @Schema(description = "New projected project completion date", example = "2027-02-15")
    private LocalDate targetCompletionDate;

    @Schema(description = "Requested net time extension in calendar days", example = "25")
    private Integer timeExtensionDays;

    @Schema(description = "Estimated cost impact of the delay/revision", example = "450000.00")
    private BigDecimal costImpact;

    @Schema(description = "Optional initial list of activity schedule adjustments to include in this revision")
    private List<@Valid CreateScheduleRevisionItemRequest> items;
}
