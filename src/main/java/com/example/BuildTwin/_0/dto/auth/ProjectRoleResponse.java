package com.example.BuildTwin._0.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Project Role Assignment Response")
public class ProjectRoleResponse {

    @Schema(description = "Assignment ID", example = "1")
    private Long id;

    @Schema(description = "User ID", example = "2")
    private Long userId;

    @Schema(description = "Username", example = "suresh_engineer")
    private String username;

    @Schema(description = "User Email", example = "suresh@buildtwin360.com")
    private String userEmail;

    @Schema(description = "Project ID", example = "1")
    private Long projectId;

    @Schema(description = "Project Name (if loaded)", example = "Padur Residence - PRJ-001")
    private String projectName;

    @Schema(description = "Role ID", example = "3")
    private Long roleId;

    @Schema(description = "Role Name", example = "ROLE_SITE_ENGINEER")
    private String roleName;

    @Schema(description = "Assignment timestamp")
    private LocalDateTime createdAt;
}
