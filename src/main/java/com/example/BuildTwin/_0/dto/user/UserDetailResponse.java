package com.example.BuildTwin._0.dto.user;

import com.example.BuildTwin._0.dto.auth.ProjectRoleResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Detailed User Profile Response")
public class UserDetailResponse {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "Unique Username", example = "karthik_pm")
    private String username;

    @Schema(description = "Email Address", example = "karthik.pm@ashokbuilders.com")
    private String email;

    @Schema(description = "Account Status", example = "ACTIVE")
    private String status;

    @Schema(description = "Assigned Global System Roles", example = "[\"ROLE_PROJECT_MANAGER\"]")
    private Set<String> roles;

    @Schema(description = "Project-specific roles assigned to this user")
    private List<ProjectRoleResponse> projectRoles;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
