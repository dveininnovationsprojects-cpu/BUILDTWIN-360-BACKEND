package com.example.BuildTwin._0.domain.procurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for processing purchase order financial approval or rejection")
public class PurchaseOrderApprovalDto {

    @NotBlank(message = "Approval status is required (APPROVED, REJECTED, or ISSUED)")
    @Schema(description = "Approval status: APPROVED, REJECTED, or ISSUED", example = "APPROVED")
    private String status;

    @Schema(description = "Authorized Project Manager / Director name", example = "Director_Murugan")
    private String approvedBy;

    @Schema(description = "Reason for rejection if status is REJECTED", example = "Amount exceeds quarterly material budget cap")
    private String rejectionReason;

    @Schema(description = "Financial approval remarks or terms", example = "Approved with 30-day credit period terms")
    private String remarks;
}
