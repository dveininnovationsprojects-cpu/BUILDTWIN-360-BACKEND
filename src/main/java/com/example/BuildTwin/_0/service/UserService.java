package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.user.*;

import java.util.Set;

public interface UserService {

    UserDetailResponse createUser(CreateUserRequest request, String performedBy);

    PageResponse<UserDetailResponse> getAllUsers(String search, String status, int page, int size, String sortBy, String sortDir);

    UserDetailResponse getUserById(Long id);

    UserDetailResponse getUserByUsername(String username);

    UserDetailResponse updateUser(Long id, UpdateUserRequest request, String performedBy);

    void deleteUser(Long id, String performedBy);

    UserDetailResponse updateUserStatus(Long id, UpdateUserStatusRequest request, String performedBy);

    void resetPassword(Long id, ResetPasswordRequest request, String performedBy);

    void changePassword(String username, ChangePasswordRequest request);

    UserDetailResponse addRolesToUser(Long id, Set<String> roles, String performedBy);

    UserDetailResponse removeRoleFromUser(Long id, String roleName, String performedBy);
}
