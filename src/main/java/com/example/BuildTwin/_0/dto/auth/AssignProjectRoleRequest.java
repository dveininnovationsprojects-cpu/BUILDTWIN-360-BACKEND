package com.example.BuildTwin._0.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Assign project-specific role payload")
public class AssignProjectRoleRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "Target User ID", example = "2")
    private Long userId;

    @NotNull(message = "Project ID is required")
    @Schema(description = "Target Project ID", example = "1")
    private Long projectId;

    @NotNull(message = "Role ID is required")
    @Schema(description = "Target Role ID", example = "3")
    private Long roleId;
}
