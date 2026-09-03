package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.auth.ProjectRoleResponse;
import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.project.*;
import com.example.BuildTwin._0.dto.site.SiteResponse;
import com.example.BuildTwin._0.exception.BadRequestException;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.*;
import com.example.BuildTwin._0.repository.*;
import com.example.BuildTwin._0.service.AuditService;
import com.example.BuildTwin._0.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private static final Set<String> ALLOWED_PROJECT_STATUSES = Set.of(
            "PLANNED", "ACTIVE", "INACTIVE", "ON_HOLD", "COMPLETED", "ARCHIVED"
    );

    private static final Set<String> ALLOWED_PROJECT_TYPES = Set.of(
            "RESIDENTIAL", "COMMERCIAL", "INFRASTRUCTURE", "INDUSTRIAL", "MIXED_USE"
    );

    private final ProjectRepository projectRepository;
    private final SiteRepository siteRepository;
    private final UserRepository userRepository;
    private final UserProjectRoleRepository userProjectRoleRepository;
    private final RoleRepository roleRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, String performedBy) {
        String code = request.getCode().trim().toUpperCase();
        if (projectRepository.existsByCode(code)) {
            throw new DuplicateResourceException("Project", "code", code);
        }

        if (request.getProjectManagerId() != null && !userRepository.existsById(request.getProjectManagerId())) {
            throw new ResourceNotFoundException("User (Project Manager)", "id", request.getProjectManagerId());
        }

        String projectStatus = request.getStatus() != null ? validateAndNormalizeStatus(request.getStatus()) : "ACTIVE";
        String projectType = request.getProjectType() != null ? validateAndNormalizeProjectType(request.getProjectType()) : "RESIDENTIAL";

        Project project = Project.builder()
                .name(request.getName().trim())
                .code(code)
                .description(request.getDescription())
                .clientName(request.getClientName() != null ? request.getClientName().trim() : "Ashok Builders & Developers")
                .projectType(projectType)
                .location(request.getLocation())
                .status(projectStatus)
                .plannedStartDate(request.getPlannedStartDate())
                .plannedEndDate(request.getPlannedEndDate())
                .actualStartDate(request.getActualStartDate())
                .estimatedBudget(request.getEstimatedBudget() != null ? request.getEstimatedBudget() : BigDecimal.ZERO)
                .currency(request.getCurrency() != null ? request.getCurrency().toUpperCase() : "INR")
                .totalBuiltUpAreaSqFt(request.getTotalBuiltUpAreaSqFt())
                .projectManagerId(request.getProjectManagerId())
                .build();

        Project savedProject = projectRepository.save(project);

        // If a project manager was specified, automatically register the project role assignment
        if (savedProject.getProjectManagerId() != null) {
            roleRepository.findByName("ROLE_PROJECT_MANAGER").ifPresent(pmRole -> {
                if (!userProjectRoleRepository.existsByUserIdAndProjectIdAndRoleId(
                        savedProject.getProjectManagerId(), savedProject.getId(), pmRole.getId())) {
                    UserProjectRole upr = UserProjectRole.builder()
                            .userId(savedProject.getProjectManagerId())
                            .projectId(savedProject.getId())
                            .roleId(pmRole.getId())
                            .build();
                    userProjectRoleRepository.save(upr);
                }
            });
        }

        auditService.logAction(
                performedBy,
                "CREATE_PROJECT",
                "PROJECT",
                String.valueOf(savedProject.getId()),
                "Created project: " + savedProject.getName() + " (" + savedProject.getCode() + ") with status " + savedProject.getStatus(),
                null
        );

        return mapToProjectResponse(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> getAllProjects(
            String search, String status, String projectType, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Project> projectPage;
        boolean hasSearch = (search != null && !search.trim().isEmpty());
        boolean hasStatus = (status != null && !status.trim().isEmpty());
        boolean hasType = (projectType != null && !projectType.trim().isEmpty());

        if (hasSearch && hasStatus) {
            String q = search.trim();
            String st = status.trim().toUpperCase();
            projectPage = projectRepository.findByStatusAndNameContainingIgnoreCaseOrStatusAndCodeContainingIgnoreCaseOrStatusAndLocationContainingIgnoreCase(
                    st, q, st, q, st, q, pageable);
        } else if (hasSearch) {
            String q = search.trim();
            projectPage = projectRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCaseOrLocationContainingIgnoreCase(q, q, q, pageable);
        } else if (hasStatus && hasType) {
            projectPage = projectRepository.findByStatusAndProjectType(status.trim().toUpperCase(), projectType.trim().toUpperCase(), pageable);
        } else if (hasStatus) {
            projectPage = projectRepository.findByStatus(status.trim().toUpperCase(), pageable);
        } else if (hasType) {
            projectPage = projectRepository.findByProjectType(projectType.trim().toUpperCase(), pageable);
        } else {
            projectPage = projectRepository.findAll(pageable);
        }

        List<ProjectResponse> content = projectPage.getContent().stream()
                .map(this::mapToProjectResponse)
                .collect(Collectors.toList());

        return PageResponse.<ProjectResponse>builder()
                .content(content)
                .pageNumber(projectPage.getNumber())
                .pageSize(projectPage.getSize())
                .totalElements(projectPage.getTotalElements())
                .totalPages(projectPage.getTotalPages())
                .isFirst(projectPage.isFirst())
                .isLast(projectPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        return mapToProjectDetailResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDetailResponse getProjectByCode(String code) {
        Project project = projectRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "code", code));
        return mapToProjectDetailResponse(project);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long id, UpdateProjectRequest request, String performedBy) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        String newCode = request.getCode().trim().toUpperCase();
        if (!project.getCode().equalsIgnoreCase(newCode) && projectRepository.existsByCodeAndIdNot(newCode, id)) {
            throw new DuplicateResourceException("Project", "code", newCode);
        }

        if (request.getProjectManagerId() != null && !userRepository.existsById(request.getProjectManagerId())) {
            throw new ResourceNotFoundException("User (Project Manager)", "id", request.getProjectManagerId());
        }

        project.setName(request.getName().trim());
        project.setCode(newCode);
        project.setDescription(request.getDescription());
        if (request.getClientName() != null) project.setClientName(request.getClientName().trim());
        if (request.getProjectType() != null) project.setProjectType(validateAndNormalizeProjectType(request.getProjectType()));
        project.setLocation(request.getLocation());
        if (request.getStatus() != null) project.setStatus(validateAndNormalizeStatus(request.getStatus()));
        project.setPlannedStartDate(request.getPlannedStartDate());
        project.setPlannedEndDate(request.getPlannedEndDate());
        project.setActualStartDate(request.getActualStartDate());
        project.setActualEndDate(request.getActualEndDate());
        if (request.getEstimatedBudget() != null) project.setEstimatedBudget(request.getEstimatedBudget());
        if (request.getCurrency() != null) project.setCurrency(request.getCurrency().toUpperCase());
        project.setTotalBuiltUpAreaSqFt(request.getTotalBuiltUpAreaSqFt());
        project.setProjectManagerId(request.getProjectManagerId());

        Project updatedProject = projectRepository.save(project);

        auditService.logAction(
                performedBy,
                "UPDATE_PROJECT",
                "PROJECT",
                String.valueOf(updatedProject.getId()),
                "Updated project: " + updatedProject.getName() + " (" + updatedProject.getCode() + ")",
                null
        );

        return mapToProjectResponse(updatedProject);
    }

    @Override
    @Transactional
    public ProjectResponse updateProjectStatus(Long id, UpdateProjectStatusRequest request, String performedBy) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        String oldStatus = project.getStatus();
        String validatedStatus = validateAndNormalizeStatus(request.getStatus());

        project.setStatus(validatedStatus);
        Project updated = projectRepository.save(project);

        auditService.logAction(
                performedBy,
                "UPDATE_PROJECT_STATUS",
                "PROJECT",
                String.valueOf(id),
                "Changed status of project '" + project.getName() + "' from " + oldStatus + " to " + updated.getStatus(),
                null
        );

        return mapToProjectResponse(updated);
    }

    @Override
    @Transactional
    public void deleteProject(Long id, String performedBy) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        // Delete associated project roles first
        userProjectRoleRepository.deleteByProjectId(id);

        String projectName = project.getName();
        String projectCode = project.getCode();

        projectRepository.delete(project);

        auditService.logAction(
                performedBy,
                "DELETE_PROJECT",
                "PROJECT",
                String.valueOf(id),
                "Deleted project: " + projectName + " (" + projectCode + ")",
                null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectMetricsResponse getProjectMetrics() {
        long total = projectRepository.count();
        long active = projectRepository.countByStatus("ACTIVE");
        long planned = projectRepository.countByStatus("PLANNED");
        long onHold = projectRepository.countByStatus("ON_HOLD");
        long completed = projectRepository.countByStatus("COMPLETED");
        BigDecimal totalBudget = projectRepository.sumTotalEstimatedBudget();
        long totalSites = siteRepository.count();

        return ProjectMetricsResponse.builder()
                .totalProjects(total)
                .activeProjects(active)
                .plannedProjects(planned)
                .onHoldProjects(onHold)
                .completedProjects(completed)
                .totalEstimatedBudget(totalBudget != null ? totalBudget : BigDecimal.ZERO)
                .totalSites(totalSites)
                .build();
    }

    private String validateAndNormalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new BadRequestException("Project status cannot be blank. Allowed statuses: " + ALLOWED_PROJECT_STATUSES);
        }
        String normalized = status.trim().toUpperCase();
        if (!ALLOWED_PROJECT_STATUSES.contains(normalized)) {
            throw new BadRequestException("Invalid project status: '" + status + "'. Allowed statuses are: " + ALLOWED_PROJECT_STATUSES);
        }
        return normalized;
    }

    private String validateAndNormalizeProjectType(String projectType) {
        if (projectType == null || projectType.trim().isEmpty()) {
            throw new BadRequestException("Project type cannot be blank. Allowed types: " + ALLOWED_PROJECT_TYPES);
        }
        String normalized = projectType.trim().toUpperCase();
        if (!ALLOWED_PROJECT_TYPES.contains(normalized)) {
            throw new BadRequestException("Invalid project type: '" + projectType + "'. Allowed types are: " + ALLOWED_PROJECT_TYPES);
        }
        return normalized;
    }

    private ProjectResponse mapToProjectResponse(Project project) {
        String pmName = null;
        if (project.getProjectManagerId() != null) {
            pmName = userRepository.findById(project.getProjectManagerId())
                    .map(User::getUsername)
                    .orElse(null);
        }

        int sitesCount = (int) siteRepository.countByProjectId(project.getId());

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .code(project.getCode())
                .description(project.getDescription())
                .clientName(project.getClientName())
                .projectType(project.getProjectType())
                .location(project.getLocation())
                .status(project.getStatus())
                .plannedStartDate(project.getPlannedStartDate())
                .plannedEndDate(project.getPlannedEndDate())
                .actualStartDate(project.getActualStartDate())
                .actualEndDate(project.getActualEndDate())
                .estimatedBudget(project.getEstimatedBudget())
                .currency(project.getCurrency())
                .totalBuiltUpAreaSqFt(project.getTotalBuiltUpAreaSqFt())
                .projectManagerId(project.getProjectManagerId())
                .projectManagerName(pmName)
                .totalSitesCount(sitesCount)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    private ProjectDetailResponse mapToProjectDetailResponse(Project project) {
        String pmName = null;
        if (project.getProjectManagerId() != null) {
            pmName = userRepository.findById(project.getProjectManagerId())
                    .map(User::getUsername)
                    .orElse(null);
        }

        List<SiteResponse> sites = siteRepository.findByProjectId(project.getId()).stream()
                .map(s -> SiteResponse.builder()
                        .id(s.getId())
                        .projectId(project.getId())
                        .projectName(project.getName())
                        .code(s.getCode())
                        .name(s.getName())
                        .siteType(s.getSiteType())
                        .location(s.getLocation())
                        .status(s.getStatus())
                        .latitude(s.getLatitude())
                        .longitude(s.getLongitude())
                        .areaSqFt(s.getAreaSqFt())
                        .siteIncharge(s.getSiteIncharge())
                        .createdAt(s.getCreatedAt())
                        .updatedAt(s.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        List<ProjectRoleResponse> teamMembers = userProjectRoleRepository.findByProjectId(project.getId()).stream()
                .map(upr -> {
                    Optional<User> userOpt = userRepository.findById(upr.getUserId());
                    Optional<Role> roleOpt = roleRepository.findById(upr.getRoleId());
                    return ProjectRoleResponse.builder()
                            .id(upr.getId())
                            .userId(upr.getUserId())
                            .username(userOpt.map(User::getUsername).orElse("Unknown"))
                            .userEmail(userOpt.map(User::getEmail).orElse("Unknown"))
                            .projectId(project.getId())
                            .projectName(project.getName())
                            .roleId(upr.getRoleId())
                            .roleName(roleOpt.map(Role::getName).orElse("Unknown"))
                            .createdAt(upr.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return ProjectDetailResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .code(project.getCode())
                .description(project.getDescription())
                .clientName(project.getClientName())
                .projectType(project.getProjectType())
                .location(project.getLocation())
                .status(project.getStatus())
                .plannedStartDate(project.getPlannedStartDate())
                .plannedEndDate(project.getPlannedEndDate())
                .actualStartDate(project.getActualStartDate())
                .actualEndDate(project.getActualEndDate())
                .estimatedBudget(project.getEstimatedBudget())
                .currency(project.getCurrency())
                .totalBuiltUpAreaSqFt(project.getTotalBuiltUpAreaSqFt())
                .projectManagerId(project.getProjectManagerId())
                .projectManagerName(pmName)
                .sites(sites)
                .teamMembers(teamMembers)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
