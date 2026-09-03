package com.example.BuildTwin._0.controller;

import com.example.BuildTwin._0.dto.ApiResponse;
import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.user.*;
import com.example.BuildTwin._0.service.UserService;
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

import java.util.Set;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "1. Identity & Access Management", description = "User Profile CRUD, Account Lifecycle, System Roles & Access Control")
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Create user (Admin / Director)",
            description = "Creates a new user profile with pre-assigned roles and status."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserDetailResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN or DIRECTOR role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ResponseEntity<ApiResponse<UserDetailResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        UserDetailResponse created = userService.createUser(request, authentication.getName());
        return new ResponseEntity<>(ApiResponse.created(created, "User created successfully"), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "List users with pagination and search",
            description = "Retrieves users list with optional search by username/email, status filter, and pagination."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Users list retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<ApiResponse<PageResponse<UserDetailResponse>>> getAllUsers(
            @Parameter(description = "Search keyword in username or email") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by status (ACTIVE, INACTIVE, SUSPENDED, PENDING_APPROVAL, REJECTED)") @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDir) {
        PageResponse<UserDetailResponse> users = userService.getAllUsers(search, status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @GetMapping("/pending-approvals")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR')")
    @Operation(
            summary = "List pending user registrations (Admin / Director)",
            description = "Fetches a list of newly registered users whose accounts are awaiting review and approval."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pending registrations retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN or DIRECTOR role")
    })
    public ResponseEntity<ApiResponse<PageResponse<UserDetailResponse>>> getPendingApprovals(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        PageResponse<UserDetailResponse> pendingUsers = userService.getPendingApprovalUsers(page, size);
        return ResponseEntity.ok(ApiResponse.success(pendingUsers, "Pending user registrations retrieved successfully"));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Approve user registration",
            description = "Approves a pending user registration, sets status to ACTIVE, optionally configures roles or project assignment."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User approved successfully",
                    content = @Content(schema = @Schema(implementation = UserDetailResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or user not in pending state"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserDetailResponse>> approveUser(
            @PathVariable Long id,
            @RequestBody(required = false) ApproveUserRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        UserDetailResponse approved = userService.approveUser(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(approved, "User registration approved successfully. Account is now ACTIVE."));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Reject user registration",
            description = "Rejects a pending user registration request with recorded reason."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User registration rejected successfully",
                    content = @Content(schema = @Schema(implementation = UserDetailResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or primary admin target"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserDetailResponse>> rejectUser(
            @PathVariable Long id,
            @Valid @RequestBody RejectUserRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        UserDetailResponse rejected = userService.rejectUser(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(rejected, "User registration rejected successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Get user by ID",
            description = "Fetches complete details of a specific user including global system roles and project-level assignments."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserDetailResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserById(@PathVariable Long id) {
        UserDetailResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User details retrieved successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Update user details",
            description = "Updates email, account status, and assigned system roles for a user."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(schema = @Schema(implementation = UserDetailResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already in use")
    })
    public ResponseEntity<ApiResponse<UserDetailResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        UserDetailResponse updated = userService.updateUser(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "User updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Delete user",
            description = "Permanently removes a user account and associated project roles (Primary admin protected)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot delete primary admin"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {
        userService.deleteUser(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Activate / Deactivate / Suspend user",
            description = "Changes user account status (ACTIVE, INACTIVE, SUSPENDED, PENDING_APPROVAL, REJECTED)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserDetailResponse>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        UserDetailResponse updated = userService.updateUserStatus(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "User status updated successfully"));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Reset user password",
            description = "Administrative override to reset a user's password without knowing old password."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        userService.resetPassword(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "User password reset successfully"));
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Add roles to user",
            description = "Assigns additional system roles to an existing user."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Roles added successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or role not found")
    })
    public ResponseEntity<ApiResponse<UserDetailResponse>> addRolesToUser(
            @PathVariable Long id,
            @RequestBody Set<String> roles,
            @Parameter(hidden = true) Authentication authentication) {
        UserDetailResponse updated = userService.addRolesToUser(id, roles, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Roles added successfully"));
    }

    @DeleteMapping("/{id}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTOR')")
    @Operation(
            summary = "Remove role from user",
            description = "Removes a specific system role from a user."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role removed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserDetailResponse>> removeRoleFromUser(
            @PathVariable Long id,
            @PathVariable String roleName,
            @Parameter(hidden = true) Authentication authentication) {
        UserDetailResponse updated = userService.removeRoleFromUser(id, roleName, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(updated, "Role removed successfully"));
    }
}
