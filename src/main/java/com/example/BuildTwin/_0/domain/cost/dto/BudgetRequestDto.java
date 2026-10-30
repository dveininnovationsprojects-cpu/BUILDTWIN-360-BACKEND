package com.example.BuildTwin._0.domain.cost.dto;

import com.example.BuildTwin._0.domain.cost.enums.CostHead;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetRequestDto {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private Long activityId;

    private Long wbsActivityId;

    @NotBlank(message = "Cost code is required")
    private String costCode;

    private CostHead costHead;

    private String description;

    private BigDecimal baselineAmount;

    private BigDecimal allocatedAmount;

    @Builder.Default
    private String currency = "INR";

    private BigDecimal revisedAmount;

    private String revisionReason;

    private String approvedBy;

    public Long getActivityId() {
        return activityId != null ? activityId : wbsActivityId;
    }

    public Long getWbsActivityId() {
        return wbsActivityId != null ? wbsActivityId : activityId;
    }

    public BigDecimal getBaselineAmount() {
        return baselineAmount != null ? baselineAmount : allocatedAmount;
    }

    public BigDecimal getAllocatedAmount() {
        return allocatedAmount != null ? allocatedAmount : baselineAmount;
    }

    public void setWbsActivityId(Long wbsActivityId) {
        this.wbsActivityId = wbsActivityId;
        if (this.activityId == null) {
            this.activityId = wbsActivityId;
        }
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
        if (this.wbsActivityId == null) {
            this.wbsActivityId = activityId;
        }
    }

    public void setAllocatedAmount(BigDecimal allocatedAmount) {
        this.allocatedAmount = allocatedAmount;
        if (this.baselineAmount == null) {
            this.baselineAmount = allocatedAmount;
        }
    }

    public void setBaselineAmount(BigDecimal baselineAmount) {
        this.baselineAmount = baselineAmount;
        if (this.allocatedAmount == null) {
            this.allocatedAmount = baselineAmount;
        }
    }

    @AssertTrue(message = "Baseline amount or allocated amount must be provided and non-negative")
    public boolean isValidAmount() {
        BigDecimal amt = getBaselineAmount();
        return amt != null && amt.compareTo(BigDecimal.ZERO) >= 0;
    }
}
