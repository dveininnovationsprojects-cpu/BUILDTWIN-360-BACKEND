package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.auth.*;
import com.example.BuildTwin._0.dto.user.ChangePasswordRequest;
import com.example.BuildTwin._0.model.Role;
import com.example.BuildTwin._0.model.UserProjectRole;
import com.example.BuildTwin._0.service.AuthService;
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
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "1. Identity & Access Management", description = "Authentication, User Management, RBAC & Project-Specific Role Assignments")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Register new user",
            description = "Creates a new user profile with password encryption and assigns default or requested roles."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request payload or validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return new ResponseEntity<>(ApiResponse.created(authResponse, "User registered successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticates user credentials and returns JWT Access Token and Refresh Token."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid username or password")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Generates a new JWT access token using a valid non-expired refresh token."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse authResponse = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Access token refreshed successfully"));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current user profile",
            description = "Fetches the profile details of the currently authenticated user extracted from the JWT Bearer token.",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserSummaryDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid")
    })
    public ResponseEntity<ApiResponse<UserSummaryDto>> getCurrentUser(
            @Parameter(hidden = true) Authentication authentication) {
        UserSummaryDto userProfile = authService.getCurrentUserProfile(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(userProfile, "Current user profile retrieved successfully"));
    }

    @PostMapping("/change-password")
    @Operation(
            summary = "Change own password",
            description = "Allows an authenticated user to change their own password by verifying current password.",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid payload or password requirements not met"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Current password does not match or unauthorized")
    })
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    @PostMapping("/assign-project-role")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Assign project role",
            description = "Assigns a specific role to a user for a particular construction project.",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Project role assigned successfully",
                    content = @Content(schema = @Schema(implementation = UserProjectRole.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN or PROJECT_MANAGER role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or Role not found")
    })
    public ResponseEntity<ApiResponse<UserProjectRole>> assignProjectRole(
            @Valid @RequestBody AssignProjectRoleRequest request) {
        UserProjectRole userProjectRole = authService.assignProjectRole(request);
        return new ResponseEntity<>(ApiResponse.created(userProjectRole, "Project role assigned successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "List all users",
            description = "Retrieves all registered users along with their assigned system roles.",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Users list retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ApiResponse<List<UserSummaryDto>>> getAllUsers() {
        List<UserSummaryDto> users = authService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Users list retrieved successfully"));
    }

    @GetMapping("/roles")
    @Operation(
            summary = "List all roles",
            description = "Retrieves master list of all standard RBAC system roles in BuildTwin 360.",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Roles list retrieved successfully"
            )
    })
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        List<Role> roles = authService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles, "System roles retrieved successfully"));
    }
}
