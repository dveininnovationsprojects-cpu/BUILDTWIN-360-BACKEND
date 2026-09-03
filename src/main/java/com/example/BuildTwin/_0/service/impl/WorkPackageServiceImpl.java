package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.wbs.CreateWorkPackageRequest;
import com.example.BuildTwin._0.dto.wbs.UpdateWorkPackageRequest;
import com.example.BuildTwin._0.dto.wbs.UpdateWorkPackageStatusRequest;
import com.example.BuildTwin._0.dto.wbs.WorkPackageResponse;
import com.example.BuildTwin._0.exception.BadRequestException;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.Project;
import com.example.BuildTwin._0.model.Site;
import com.example.BuildTwin._0.model.User;
import com.example.BuildTwin._0.model.WorkPackage;
import com.example.BuildTwin._0.repository.ProjectRepository;
import com.example.BuildTwin._0.repository.SiteRepository;
import com.example.BuildTwin._0.repository.UserRepository;
import com.example.BuildTwin._0.repository.WorkPackageRepository;
import com.example.BuildTwin._0.service.AuditService;
import com.example.BuildTwin._0.service.WorkPackageService;
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
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkPackageServiceImpl implements WorkPackageService {

    private static final Set<String> ALLOWED_WP_STATUSES = Set.of(
            "PLANNED", "IN_PROGRESS", "ON_HOLD", "COMPLETED", "CANCELLED"
    );

    private static final Set<String> ALLOWED_DISCIPLINES = Set.of(
            "CIVIL", "STRUCTURAL", "MEP", "ELECTRICAL", "PLUMBING",
            "HVAC", "FINISHING", "FIRE_FIGHTING", "WATERPROOFING", "INFRASTRUCTURE"
    );

    private final WorkPackageRepository workPackageRepository;
    private final ProjectRepository projectRepository;
    private final SiteRepository siteRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public WorkPackageResponse createWorkPackage(Long projectId, CreateWorkPackageRequest request, String performedBy) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        String code = request.getCode().trim().toUpperCase();
        if (workPackageRepository.existsByProjectIdAndCode(projectId, code)) {
            throw new DuplicateResourceException("WorkPackage", "code", code + " in Project " + project.getName());
        }

        Site site = null;
        if (request.getSiteId() != null) {
            site = siteRepository.findById(request.getSiteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Site", "id", request.getSiteId()));
            if (!site.getProject().getId().equals(projectId)) {
                throw new BadRequestException("Site ID " + request.getSiteId() + " does not belong to Project ID " + projectId);
            }
        }

        if (request.getInchargeUserId() != null && !userRepository.existsById(request.getInchargeUserId())) {
            throw new ResourceNotFoundException("User (Incharge)", "id", request.getInchargeUserId());
        }

        String status = request.getStatus() != null ? validateAndNormalizeStatus(request.getStatus()) : "PLANNED";
        String discipline = validateAndNormalizeDiscipline(request.getDiscipline());

        WorkPackage wp = WorkPackage.builder()
                .project(project)
                .site(site)
                .code(code)
                .name(request.getName().trim())
                .discipline(discipline)
                .description(request.getDescription())
                .status(status)
                .plannedStartDate(request.getPlannedStartDate())
                .plannedEndDate(request.getPlannedEndDate())
                .actualStartDate(request.getActualStartDate())
                .budgetAmount(request.getBudgetAmount() != null ? request.getBudgetAmount() : BigDecimal.ZERO)
                .assignedContractor(request.getAssignedContractor())
                .inchargeUserId(request.getInchargeUserId())
                .build();

        WorkPackage saved = workPackageRepository.save(wp);

        auditService.logAction(
                performedBy,
                "CREATE_WORK_PACKAGE",
                "WORK_PACKAGE",
                String.valueOf(saved.getId()),
                "Created work package: " + saved.getName() + " (" + saved.getCode() + ") under project: " + project.getName(),
                null
        );

        return mapToWorkPackageResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WorkPackageResponse> getWorkPackagesByProjectId(
            Long projectId, String status, String discipline, int page, int size, String sortBy, String sortDir) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<WorkPackage> wpPage;
        if (status != null && !status.trim().isEmpty()) {
            wpPage = workPackageRepository.findByProjectIdAndStatus(projectId, status.trim().toUpperCase(), pageable);
        } else if (discipline != null && !discipline.trim().isEmpty()) {
            wpPage = workPackageRepository.findByProjectIdAndDiscipline(projectId, discipline.trim().toUpperCase(), pageable);
        } else {
            wpPage = workPackageRepository.findByProjectId(projectId, pageable);
        }

        List<WorkPackageResponse> content = wpPage.getContent().stream()
                .map(this::mapToWorkPackageResponse)
                .collect(Collectors.toList());

        return PageResponse.<WorkPackageResponse>builder()
                .content(content)
                .pageNumber(wpPage.getNumber())
                .pageSize(wpPage.getSize())
                .totalElements(wpPage.getTotalElements())
                .totalPages(wpPage.getTotalPages())
                .isFirst(wpPage.isFirst())
                .isLast(wpPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkPackageResponse getWorkPackageById(Long id) {
        WorkPackage wp = workPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkPackage", "id", id));
        return mapToWorkPackageResponse(wp);
    }

    @Override
    @Transactional
    public WorkPackageResponse updateWorkPackage(Long id, UpdateWorkPackageRequest request, String performedBy) {
        WorkPackage wp = workPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkPackage", "id", id));

        String newCode = request.getCode().trim().toUpperCase();
        if (!wp.getCode().equalsIgnoreCase(newCode) &&
                workPackageRepository.existsByProjectIdAndCodeAndIdNot(wp.getProject().getId(), newCode, id)) {
            throw new DuplicateResourceException("WorkPackage", "code", newCode + " in Project " + wp.getProject().getName());
        }

        if (request.getSiteId() != null) {
            Site site = siteRepository.findById(request.getSiteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Site", "id", request.getSiteId()));
            if (!site.getProject().getId().equals(wp.getProject().getId())) {
                throw new BadRequestException("Site ID " + request.getSiteId() + " does not belong to Project ID " + wp.getProject().getId());
            }
            wp.setSite(site);
        } else {
            wp.setSite(null);
        }

        if (request.getInchargeUserId() != null && !userRepository.existsById(request.getInchargeUserId())) {
            throw new ResourceNotFoundException("User (Incharge)", "id", request.getInchargeUserId());
        }

        wp.setCode(newCode);
        wp.setName(request.getName().trim());
        wp.setDiscipline(validateAndNormalizeDiscipline(request.getDiscipline()));
        wp.setDescription(request.getDescription());
        if (request.getStatus() != null) wp.setStatus(validateAndNormalizeStatus(request.getStatus()));
        wp.setPlannedStartDate(request.getPlannedStartDate());
        wp.setPlannedEndDate(request.getPlannedEndDate());
        wp.setActualStartDate(request.getActualStartDate());
        wp.setActualEndDate(request.getActualEndDate());
        if (request.getBudgetAmount() != null) wp.setBudgetAmount(request.getBudgetAmount());
        wp.setAssignedContractor(request.getAssignedContractor());
        wp.setInchargeUserId(request.getInchargeUserId());

        WorkPackage updated = workPackageRepository.save(wp);

        auditService.logAction(
                performedBy,
                "UPDATE_WORK_PACKAGE",
                "WORK_PACKAGE",
                String.valueOf(updated.getId()),
                "Updated work package: " + updated.getName() + " (" + updated.getCode() + ")",
                null
        );

        return mapToWorkPackageResponse(updated);
    }

    @Override
    @Transactional
    public WorkPackageResponse updateWorkPackageStatus(Long id, UpdateWorkPackageStatusRequest request, String performedBy) {
        WorkPackage wp = workPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkPackage", "id", id));

        String oldStatus = wp.getStatus();
        String validatedStatus = validateAndNormalizeStatus(request.getStatus());

        wp.setStatus(validatedStatus);
        WorkPackage updated = workPackageRepository.save(wp);

        auditService.logAction(
                performedBy,
                "UPDATE_WORK_PACKAGE_STATUS",
                "WORK_PACKAGE",
                String.valueOf(id),
                "Changed status of work package '" + wp.getName() + "' from " + oldStatus + " to " + updated.getStatus(),
                null
        );

        return mapToWorkPackageResponse(updated);
    }

    @Override
    @Transactional
    public void deleteWorkPackage(Long id, String performedBy) {
        WorkPackage wp = workPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkPackage", "id", id));

        String name = wp.getName();
        workPackageRepository.delete(wp);

        auditService.logAction(
                performedBy,
                "DELETE_WORK_PACKAGE",
                "WORK_PACKAGE",
                String.valueOf(id),
                "Deleted work package: " + name,
                null
        );
    }

    private String validateAndNormalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new BadRequestException("Status cannot be blank. Allowed statuses: " + ALLOWED_WP_STATUSES);
        }
        String normalized = status.trim().toUpperCase();
        if (!ALLOWED_WP_STATUSES.contains(normalized)) {
            throw new BadRequestException("Invalid work package status: '" + status + "'. Allowed statuses are: " + ALLOWED_WP_STATUSES);
        }
        return normalized;
    }

    private String validateAndNormalizeDiscipline(String discipline) {
        if (discipline == null || discipline.trim().isEmpty()) {
            throw new BadRequestException("Discipline cannot be blank. Allowed disciplines: " + ALLOWED_DISCIPLINES);
        }
        String normalized = discipline.trim().toUpperCase();
        if (!ALLOWED_DISCIPLINES.contains(normalized)) {
            throw new BadRequestException("Invalid discipline: '" + discipline + "'. Allowed disciplines are: " + ALLOWED_DISCIPLINES);
        }
        return normalized;
    }

    private WorkPackageResponse mapToWorkPackageResponse(WorkPackage wp) {
        String inchargeName = null;
        if (wp.getInchargeUserId() != null) {
            inchargeName = userRepository.findById(wp.getInchargeUserId())
                    .map(User::getUsername)
                    .orElse(null);
        }

        return WorkPackageResponse.builder()
                .id(wp.getId())
                .projectId(wp.getProject() != null ? wp.getProject().getId() : null)
                .projectName(wp.getProject() != null ? wp.getProject().getName() : null)
                .siteId(wp.getSite() != null ? wp.getSite().getId() : null)
                .siteName(wp.getSite() != null ? wp.getSite().getName() : null)
                .code(wp.getCode())
                .name(wp.getName())
                .discipline(wp.getDiscipline())
                .description(wp.getDescription())
                .status(wp.getStatus())
                .plannedStartDate(wp.getPlannedStartDate())
                .plannedEndDate(wp.getPlannedEndDate())
                .actualStartDate(wp.getActualStartDate())
                .actualEndDate(wp.getActualEndDate())
                .budgetAmount(wp.getBudgetAmount())
                .assignedContractor(wp.getAssignedContractor())
                .inchargeUserId(wp.getInchargeUserId())
                .inchargeUserName(inchargeName)
                .createdAt(wp.getCreatedAt())
                .updatedAt(wp.getUpdatedAt())
                .build();
    }
}
