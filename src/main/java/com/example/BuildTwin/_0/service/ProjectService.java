package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.project.*;

public interface ProjectService {

    ProjectResponse createProject(CreateProjectRequest request, String performedBy);

    PageResponse<ProjectResponse> getAllProjects(String search, String status, String projectType, int page, int size, String sortBy, String sortDir);

    ProjectDetailResponse getProjectById(Long id);

    ProjectDetailResponse getProjectByCode(String code);

    ProjectResponse updateProject(Long id, UpdateProjectRequest request, String performedBy);

    ProjectResponse updateProjectStatus(Long id, UpdateProjectStatusRequest request, String performedBy);

    void deleteProject(Long id, String performedBy);

    ProjectMetricsResponse getProjectMetrics();
}
