package com.example.BuildTwin._0.dto.dpr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Optional request payload when submitting a Daily Progress Report for review")
public class SubmitDprRequest {

    @Schema(description = "Optional submission remarks or executive handover notes from site engineer",
            example = "Shift targets achieved; 35.5 CUM poured; ready for PM sign-off.")
    private String submissionNotes;
}