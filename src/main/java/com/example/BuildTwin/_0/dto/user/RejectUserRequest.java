package com.example.BuildTwin._0.dto.user;

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
@Schema(description = "Request payload for rejecting a pending user registration")
public class RejectUserRequest {

    @NotBlank(message = "Rejection reason is required")
    @Schema(description = "Reason for registration rejection", example = "Unauthorized applicant or invalid credentials provided")
    private String reason;
}
