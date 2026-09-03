package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.model.Role;
import com.example.BuildTwin._0.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "1. Identity & Access Management", description = "Standard RBAC Roles Directory & Permissions")
@SecurityRequirement(name = "BearerAuth")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @Operation(
            summary = "List all 10 standard system roles",
            description = "Retrieves all standard RBAC roles configured according to BuildTwin 360 SRS Section 7."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Roles list retrieved successfully"
            )
    })
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles, "System roles retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get role by ID",
            description = "Fetches a specific system role by its numeric ID."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Role retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Role.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<ApiResponse<Role>> getRoleById(@PathVariable Long id) {
        Role role = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.success(role, "Role retrieved successfully"));
    }
}
