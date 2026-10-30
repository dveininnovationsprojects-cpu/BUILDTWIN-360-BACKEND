package com.example.BuildTwin._0.dto.revision;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to reject a submitted schedule revision")
public class RejectScheduleRevisionRequest {

    @NotBlank(message = "Rejection reason is required")
    @Schema(description = "Reason for rejecting this schedule revision proposal", example = "Proposed 25-day extension is unacceptable without additional site acceleration plan.")
    private String rejectionReason;
}
