package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.auth.AssignProjectRoleRequest;
import com.example.BuildTwin._0.dto.auth.ProjectRoleResponse;

import java.util.List;

public interface ProjectRoleService {
    ProjectRoleResponse assignProjectRole(AssignProjectRoleRequest request, String performedBy);
    List<ProjectRoleResponse> getProjectRolesByUserId(Long userId);
    List<ProjectRoleResponse> getMembersByProjectId(Long projectId);
    void revokeProjectRole(Long id, String performedBy);
}
