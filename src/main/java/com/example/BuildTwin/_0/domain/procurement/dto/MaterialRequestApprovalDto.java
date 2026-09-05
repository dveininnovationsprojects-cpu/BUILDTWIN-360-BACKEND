package com.example.BuildTwin._0.domain.procurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for processing material request approval or rejection")
public class MaterialRequestApprovalDto {

    @NotBlank(message = "Approval status is required (APPROVED or REJECTED)")
    @Schema(description = "Approval status: APPROVED or REJECTED", example = "APPROVED")
    private String status;

    @Schema(description = "User name / ID approving or rejecting the request", example = "PM_Selvamani")
    private String approvedBy;

    @Schema(description = "Reason for rejection if status is REJECTED", example = "Excessive quantity requested beyond weekly budget")
    private String rejectionReason;

    @Schema(description = "Approval comments or site remarks", example = "Approved for Zone A foundation pouring")
    private String remarks;
}
