package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.auth.AssignProjectRoleRequest;
import com.example.BuildTwin._0.dto.auth.ProjectRoleResponse;
import com.example.BuildTwin._0.service.ProjectRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/project-roles")
@RequiredArgsConstructor
@Tag(name = "1. Identity & Access Management", description = "Project-Specific Role Assignments & Member Access Control")
@SecurityRequirement(name = "BearerAuth")
public class ProjectRoleController {

    private final ProjectRoleService projectRoleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Assign project role",
            description = "Assigns a specific construction role to a user for a particular project."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Project role assigned successfully",
                    content = @Content(schema = @Schema(implementation = ProjectRoleResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN or PM role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User, Role, or Project not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Project role already assigned to this user")
    })
    public ResponseEntity<ApiResponse<ProjectRoleResponse>> assignProjectRole(
            @Valid @RequestBody AssignProjectRoleRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        ProjectRoleResponse response = projectRoleService.assignProjectRole(request, authentication.getName());
        return new ResponseEntity<>(ApiResponse.created(response, "Project role assigned successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('DIRECTOR') or authentication.principal.id == #userId")
    @Operation(
            summary = "List project roles by user",
            description = "Retrieves all project assignments and roles for a specific user ID."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Project roles retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<List<ProjectRoleResponse>>> getProjectRolesByUser(@PathVariable Long userId) {
        List<ProjectRoleResponse> responses = projectRoleService.getProjectRolesByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(responses, "User project roles retrieved successfully"));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "List project members and their roles",
            description = "Retrieves all assigned team members and their respective roles for a specific project."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Project members retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponse<List<ProjectRoleResponse>>> getMembersByProject(@PathVariable Long projectId) {
        List<ProjectRoleResponse> responses = projectRoleService.getMembersByProjectId(projectId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Project team members retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Revoke project role assignment",
            description = "Removes a user's role assignment from a specific project."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project role revoked successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    public ResponseEntity<ApiResponse<Void>> revokeProjectRole(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        projectRoleService.revokeProjectRole(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Project role revoked successfully"));
    }
}
