package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.auth.AssignProjectRoleRequest;
import com.example.BuildTwin._0.dto.auth.ProjectRoleResponse;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.Project;
import com.example.BuildTwin._0.model.Role;
import com.example.BuildTwin._0.model.User;
import com.example.BuildTwin._0.model.UserProjectRole;
import com.example.BuildTwin._0.repository.ProjectRepository;
import com.example.BuildTwin._0.repository.RoleRepository;
import com.example.BuildTwin._0.repository.UserProjectRoleRepository;
import com.example.BuildTwin._0.repository.UserRepository;
import com.example.BuildTwin._0.service.AuditService;
import com.example.BuildTwin._0.service.ProjectRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectRoleServiceImpl implements ProjectRoleService {

    private final UserProjectRoleRepository userProjectRoleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProjectRepository projectRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public ProjectRoleResponse assignProjectRole(AssignProjectRoleRequest request, String performedBy) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        if (userProjectRoleRepository.existsByUserIdAndProjectIdAndRoleId(user.getId(), project.getId(), role.getId())) {
            throw new DuplicateResourceException("Project Role assignment already exists for user: " + user.getUsername() + ", project: " + project.getName() + ", role: " + role.getName());
        }

        UserProjectRole userProjectRole = UserProjectRole.builder()
                .userId(user.getId())
                .projectId(project.getId())
                .roleId(role.getId())
                .build();

        UserProjectRole saved = userProjectRoleRepository.save(userProjectRole);

        auditService.logAction(
                performedBy,
                "ASSIGN_PROJECT_ROLE",
                "USER_PROJECT_ROLE",
                String.valueOf(saved.getId()),
                "Assigned role " + role.getName() + " to user " + user.getUsername() + " for project " + project.getName(),
                null
        );

        return mapToProjectRoleResponse(saved, user, project, role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectRoleResponse> getProjectRolesByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        List<UserProjectRole> roles = userProjectRoleRepository.findByUserId(userId);
        return roles.stream().map(this::mapToProjectRoleResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectRoleResponse> getMembersByProjectId(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }

        List<UserProjectRole> roles = userProjectRoleRepository.findByProjectId(projectId);
        return roles.stream().map(this::mapToProjectRoleResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void revokeProjectRole(Long id, String performedBy) {
        UserProjectRole userProjectRole = userProjectRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserProjectRole", "id", id));

        userProjectRoleRepository.delete(userProjectRole);

        auditService.logAction(
                performedBy,
                "REVOKE_PROJECT_ROLE",
                "USER_PROJECT_ROLE",
                String.valueOf(id),
                "Revoked project role assignment ID: " + id,
                null
        );
    }

    private ProjectRoleResponse mapToProjectRoleResponse(UserProjectRole upr) {
        Optional<User> userOpt = userRepository.findById(upr.getUserId());
        Optional<Project> projectOpt = projectRepository.findById(upr.getProjectId());
        Optional<Role> roleOpt = roleRepository.findById(upr.getRoleId());

        return ProjectRoleResponse.builder()
                .id(upr.getId())
                .userId(upr.getUserId())
                .username(userOpt.map(User::getUsername).orElse("Unknown"))
                .userEmail(userOpt.map(User::getEmail).orElse("Unknown"))
                .projectId(upr.getProjectId())
                .projectName(projectOpt.map(Project::getName).orElse("Unknown"))
                .roleId(upr.getRoleId())
                .roleName(roleOpt.map(Role::getName).orElse("Unknown"))
                .createdAt(upr.getCreatedAt())
                .build();
    }

    private ProjectRoleResponse mapToProjectRoleResponse(UserProjectRole upr, User user, Project project, Role role) {
        return ProjectRoleResponse.builder()
                .id(upr.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .userEmail(user.getEmail())
                .projectId(project.getId())
                .projectName(project.getName())
                .roleId(role.getId())
                .roleName(role.getName())
                .createdAt(upr.getCreatedAt())
                .build();
    }
}
