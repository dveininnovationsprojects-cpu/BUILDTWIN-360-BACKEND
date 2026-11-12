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
@Schema(description = "Lookahead Planning & Weekly Commitments Integration Details")
public class LookaheadIntegrationDto {

    @Schema(description = "Total lookahead commitments configured", example = "18")
    private Long totalCommitments;

    @Schema(description = "Active / committed tasks count", example = "12")
    private Long activeCommitments;

    @Schema(description = "Fulfilled / completed commitments count", example = "5")
    private Long completedCommitments;

    @Schema(description = "Overdue or hindered commitments count", example = "1")
    private Long overdueCommitments;

    @Schema(description = "Lookahead planning status", example = "SYNCHRONIZED")
    private String status;
}
