package com.example.BuildTwin._0.dto.revision;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update draft schedule revision metadata")
public class UpdateScheduleRevisionRequest {

    @Schema(description = "Updated revision name", example = "Baseline Revision 1 (Updated Narrative)")
    private String revisionName;

    @Schema(description = "Updated revision date", example = "2026-10-01")
    private LocalDate revisionDate;

    @Schema(description = "Updated narrative / justification notes", example = "Refined analysis of delayed critical path activities.")
    private String reasonForRevision;

    @Schema(description = "Updated target project completion date", example = "2027-02-28")
    private LocalDate targetCompletionDate;

    @Schema(description = "Updated time extension in calendar days", example = "30")
    private Integer timeExtensionDays;

    @Schema(description = "Updated cost impact amount", example = "500000.00")
    private BigDecimal costImpact;
}
