package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.auth.ProjectRoleResponse;
import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.user.*;
import com.example.BuildTwin._0.exception.BadRequestException;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.exception.UnauthorizedException;
import com.example.BuildTwin._0.model.Project;
import com.example.BuildTwin._0.model.Role;
import com.example.BuildTwin._0.model.User;
import com.example.BuildTwin._0.model.UserProjectRole;
import com.example.BuildTwin._0.repository.ProjectRepository;
import com.example.BuildTwin._0.repository.RoleRepository;
import com.example.BuildTwin._0.repository.UserProjectRoleRepository;
import com.example.BuildTwin._0.repository.UserRepository;
import com.example.BuildTwin._0.service.AuditService;
import com.example.BuildTwin._0.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserProjectRoleRepository userProjectRoleRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Override
    @Transactional
    public UserDetailResponse createUser(CreateUserRequest request, String performedBy) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        Set<Role> assignedRoles = resolveRoles(request.getRoles());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(request.getStatus() != null ? request.getStatus().toUpperCase() : "ACTIVE")
                .roles(assignedRoles)
                .build();

        User savedUser = userRepository.save(user);

        auditService.logAction(
                performedBy,
                "CREATE_USER",
                "USER",
                String.valueOf(savedUser.getId()),
                "Created user: " + savedUser.getUsername() + " with roles: " + formatRoleNames(assignedRoles),
                null
        );

        return mapToUserDetailResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDetailResponse> getAllUsers(String search, String status, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userPage;
        boolean hasSearch = (search != null && !search.trim().isEmpty());
        boolean hasStatus = (status != null && !status.trim().isEmpty());

        if (hasSearch && hasStatus) {
            String q = search.trim();
            String st = status.trim().toUpperCase();
            userPage = userRepository.findByStatusAndUsernameContainingIgnoreCaseOrStatusAndEmailContainingIgnoreCase(st, q, st, q, pageable);
        } else if (hasSearch) {
            String q = search.trim();
            userPage = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, pageable);
        } else if (hasStatus) {
            userPage = userRepository.findByStatus(status.trim().toUpperCase(), pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        List<UserDetailResponse> content = userPage.getContent().stream()
                .map(this::mapToUserDetailResponse)
                .collect(Collectors.toList());

        return PageResponse.<UserDetailResponse>builder()
                .content(content)
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .isFirst(userPage.isFirst())
                .isLast(userPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToUserDetailResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return mapToUserDetailResponse(user);
    }

    @Override
    @Transactional
    public UserDetailResponse updateUser(Long id, UpdateUserRequest request, String performedBy) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Check email uniqueness if email changed
        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        user.setEmail(request.getEmail());

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            user.setStatus(request.getStatus().toUpperCase());
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            user.setRoles(resolveRoles(request.getRoles()));
        }

        User updatedUser = userRepository.save(user);

        auditService.logAction(
                performedBy,
                "UPDATE_USER",
                "USER",
                String.valueOf(updatedUser.getId()),
                "Updated profile for user: " + updatedUser.getUsername(),
                null
        );

        return mapToUserDetailResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id, String performedBy) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new BadRequestException("The primary administrator account ('admin') cannot be deleted");
        }

        // Clean up project role associations first
        userProjectRoleRepository.deleteByUserId(user.getId());

        userRepository.delete(user);

        auditService.logAction(
                performedBy,
                "DELETE_USER",
                "USER",
                String.valueOf(id),
                "Deleted user account: " + user.getUsername(),
                null
        );
    }

    @Override
    @Transactional
    public UserDetailResponse updateUserStatus(Long id, UpdateUserStatusRequest request, String performedBy) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if ("admin".equalsIgnoreCase(user.getUsername()) && !"ACTIVE".equalsIgnoreCase(request.getStatus())) {
            throw new BadRequestException("The primary administrator account ('admin') cannot be deactivated or suspended");
        }

        String oldStatus = user.getStatus();
        user.setStatus(request.getStatus().toUpperCase());
        User updated = userRepository.save(user);

        auditService.logAction(
                performedBy,
                "UPDATE_USER_STATUS",
                "USER",
                String.valueOf(id),
                "Changed status of user " + user.getUsername() + " from " + oldStatus + " to " + user.getStatus(),
                null
        );

        return mapToUserDetailResponse(updated);
    }

    @Override
    @Transactional
    public void resetPassword(Long id, ResetPasswordRequest request, String performedBy) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditService.logAction(
                performedBy,
                "RESET_PASSWORD",
                "USER",
                String.valueOf(id),
                "Administrator reset password for user: " + user.getUsername(),
                null
        );
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UnauthorizedException("Current password does not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditService.logAction(
                username,
                "CHANGE_PASSWORD",
                "USER",
                String.valueOf(user.getId()),
                "User successfully changed their own password",
                null
        );
    }

    @Override
    @Transactional
    public UserDetailResponse addRolesToUser(Long id, Set<String> roleNames, String performedBy) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        Set<Role> rolesToAdd = resolveRoles(roleNames);
        user.getRoles().addAll(rolesToAdd);
        User updated = userRepository.save(user);

        auditService.logAction(
                performedBy,
                "ADD_ROLES",
                "USER",
                String.valueOf(id),
                "Added roles " + formatRoleNames(rolesToAdd) + " to user: " + user.getUsername(),
                null
        );

        return mapToUserDetailResponse(updated);
    }

    @Override
    @Transactional
    public UserDetailResponse removeRoleFromUser(Long id, String roleName, String performedBy) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        String normalized = roleName.toUpperCase().startsWith("ROLE_") ? roleName.toUpperCase() : "ROLE_" + roleName.toUpperCase();
        user.getRoles().removeIf(r -> r.getName().equalsIgnoreCase(normalized));
        User updated = userRepository.save(user);

        auditService.logAction(
                performedBy,
                "REMOVE_ROLE",
                "USER",
                String.valueOf(id),
                "Removed role " + normalized + " from user: " + user.getUsername(),
                null
        );

        return mapToUserDetailResponse(updated);
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        if (roleNames != null && !roleNames.isEmpty()) {
            for (String roleName : roleNames) {
                String normalized = roleName.toUpperCase().startsWith("ROLE_") ? roleName.toUpperCase() : "ROLE_" + roleName.toUpperCase();
                Role role = roleRepository.findByName(normalized)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "name", normalized));
                roles.add(role);
            }
        } else {
            Role defaultRole = roleRepository.findByName("ROLE_SITE_ENGINEER")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_SITE_ENGINEER").build()));
            roles.add(defaultRole);
        }
        return roles;
    }

    private String formatRoleNames(Set<Role> roles) {
        return roles.stream().map(Role::getName).collect(Collectors.joining(", "));
    }

    private UserDetailResponse mapToUserDetailResponse(User user) {
        Set<String> roleNames = user.getRoles() != null
                ? user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
                : Set.of();

        List<UserProjectRole> projectRoles = userProjectRoleRepository.findByUserId(user.getId());
        List<ProjectRoleResponse> projectRoleDtos = projectRoles.stream()
                .map(upr -> {
                    Optional<Project> proj = projectRepository.findById(upr.getProjectId());
                    Optional<Role> role = roleRepository.findById(upr.getRoleId());
                    return ProjectRoleResponse.builder()
                            .id(upr.getId())
                            .userId(user.getId())
                            .username(user.getUsername())
                            .userEmail(user.getEmail())
                            .projectId(upr.getProjectId())
                            .projectName(proj.map(Project::getName).orElse("Unknown"))
                            .roleId(upr.getRoleId())
                            .roleName(role.map(Role::getName).orElse("Unknown"))
                            .createdAt(upr.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return UserDetailResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus())
                .roles(roleNames)
                .projectRoles(projectRoleDtos)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
