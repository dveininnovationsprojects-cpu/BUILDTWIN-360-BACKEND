package com.example.BuildTwin._0.domain.cost.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request DTO to record an authorized budget revision with justification")
public class BudgetRevisionDto {

    @NotNull(message = "Revised amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Revised amount must be non-negative")
    @Schema(description = "Authorized revised budget amount", example = "750000.00")
    private BigDecimal revisedAmount;

    @NotBlank(message = "Revision reason is required")
    @Schema(description = "Audit justification / change order reason for budget revision", example = "Scope expansion for floor foundation per revised engineering drawing")
    private String revisionReason;

    @Schema(description = "Director or Project Manager approving the revision", example = "Director_Murugan")
    private String approvedBy;
}
