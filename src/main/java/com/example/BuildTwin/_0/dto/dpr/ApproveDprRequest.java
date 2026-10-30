package com.example.BuildTwin._0.dto.dpr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Optional request payload when approving a Daily Progress Report")
public class ApproveDprRequest {

    @Schema(description = "Optional managerial approval notes, comments, or compliance remarks",
            example = "Approved. Cube test samples logged with QA lab; physical quantities synced to WBS.")
    private String approvalNotes;
}
