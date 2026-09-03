package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.auth.*;
import com.example.BuildTwin._0.dto.user.ChangePasswordRequest;
import com.example.BuildTwin._0.model.Role;
import com.example.BuildTwin._0.model.UserProjectRole;

import java.util.List;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    UserSummaryDto getCurrentUserProfile(String username);

    UserProjectRole assignProjectRole(AssignProjectRoleRequest request);

    void changePassword(String username, ChangePasswordRequest request);

    List<UserSummaryDto> getAllUsers();

    List<Role> getAllRoles();
}
