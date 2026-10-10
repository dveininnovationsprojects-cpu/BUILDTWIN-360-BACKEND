package com.example.BuildTwin._0.dto.dpr;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload to reject a submitted DPR with audit reason")
public class RejectDprRequest {

    @NotBlank(message = "Rejection reason is required")
    @Schema(description = "Detailed justification explaining why the DPR was returned for modification", example = "Reported concrete volume exceeds batch slip total by 15 CUM. Please reconcile with batch plant challan #402.")
    private String rejectionReason;
}
