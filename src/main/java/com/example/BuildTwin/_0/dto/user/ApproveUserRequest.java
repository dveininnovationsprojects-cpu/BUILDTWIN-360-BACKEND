package com.example.BuildTwin._0.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for approving a pending user registration")
public class ApproveUserRequest {

    @Schema(description = "Optional override or confirmation of system roles", example = "[\"ROLE_SITE_ENGINEER\"]")
    private Set<String> roles;

    @Schema(description = "Optional project ID to assign on approval", example = "1")
    private Long projectId;

    @Schema(description = "Optional remarks or approval notes", example = "Approved for Padur Site Phase 1")
    private String remarks;
}
