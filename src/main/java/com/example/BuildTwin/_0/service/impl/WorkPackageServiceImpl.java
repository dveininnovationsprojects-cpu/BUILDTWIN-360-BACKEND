


package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.wbs.CreateWorkPackageRequest;
import com.example.BuildTwin._0.dto.wbs.UpdateWorkPackageRequest;
import com.example.BuildTwin._0.dto.wbs.UpdateWorkPackageStatusRequest;
import com.example.BuildTwin._0.dto.wbs.WorkPackageResponse;
import com.example.BuildTwin._0.exception.BadRequestException;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.domain.identity.model.User;
import com.example.BuildTwin._0.domain.projects.model.Project;
import com.example.BuildTwin._0.domain.projects.model.Site;
import com.example.BuildTwin._0.domain.projects.repository.ProjectRepository;
import com.example.BuildTwin._0.model.WbsActivity;
import com.example.BuildTwin._0.model.WorkPackage;
import com.example.BuildTwin._0.repository.ActivityDependencyRepository;
import com.example.BuildTwin._0.repository.SiteRepository;
import com.example.BuildTwin._0.repository.UserRepository;
import com.example.BuildTwin._0.repository.WbsActivityRepository;
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
import java.math.RoundingMode;
import java.time.LocalDate;
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
    private final WbsActivityRepository wbsActivityRepository;
    private final ActivityDependencyRepository activityDependencyRepository;
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

        // Validate dates
        validateWorkPackageDates(request.getPlannedStartDate(), request.getPlannedEndDate(),
                request.getActualStartDate(), null, project);

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

        // Validate dates
        validateWorkPackageDates(request.getPlannedStartDate(), request.getPlannedEndDate(),
                request.getActualStartDate(), request.getActualEndDate(), wp.getProject());

        wp.setCode(newCode);
        wp.setName(request.getName().trim());
        wp.setDiscipline(validateAndNormalizeDiscipline(request.getDiscipline()));
        wp.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            String newStatus = validateAndNormalizeStatus(request.getStatus());
            if ("COMPLETED".equalsIgnoreCase(newStatus) && !"COMPLETED".equalsIgnoreCase(wp.getStatus())) {
                validateAllActivitiesCompleted(wp.getId());
            }
            wp.setStatus(newStatus);
        }
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

        if ("COMPLETED".equalsIgnoreCase(validatedStatus) && !"COMPLETED".equalsIgnoreCase(oldStatus)) {
            validateAllActivitiesCompleted(wp.getId());
            if (wp.getActualEndDate() == null) {
                wp.setActualEndDate(LocalDate.now());
            }
        } else if ("IN_PROGRESS".equalsIgnoreCase(validatedStatus) && wp.getActualStartDate() == null) {
            wp.setActualStartDate(LocalDate.now());
        }

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

        // Safely cascade delete activities and their dependencies first
        List<WbsActivity> activities = wbsActivityRepository.findByWorkPackageIdOrderBySequenceOrderAsc(id);
        for (WbsActivity act : activities) {
            activityDependencyRepository.deleteByActivityId(act.getId());
        }
        wbsActivityRepository.deleteAll(activities);

        workPackageRepository.delete(wp);

        auditService.logAction(
                performedBy,
                "DELETE_WORK_PACKAGE",
                "WORK_PACKAGE",
                String.valueOf(id),
                "Deleted work package: " + name + " and its " + activities.size() + " activities cleanly.",
                null
        );
    }

    private void validateWorkPackageDates(LocalDate plannedStart, LocalDate plannedEnd,
                                          LocalDate actualStart, LocalDate actualEnd, Project project) {
        if (plannedStart != null && plannedEnd != null && plannedEnd.isBefore(plannedStart)) {
            throw new BadRequestException("Planned end date (" + plannedEnd + ") cannot be before planned start date (" + plannedStart + ")");
        }
        if (actualStart != null && actualEnd != null && actualEnd.isBefore(actualStart)) {
            throw new BadRequestException("Actual end date (" + actualEnd + ") cannot be before actual start date (" + actualStart + ")");
        }
        if (actualEnd != null && actualStart == null) {
            throw new BadRequestException("Actual end date cannot be set without an actual start date");
        }
        if (project != null) {
            if (project.getPlannedStartDate() != null && plannedStart != null && plannedStart.isBefore(project.getPlannedStartDate())) {
                throw new BadRequestException("Work Package planned start date (" + plannedStart +
                        ") cannot be earlier than Project planned start date (" + project.getPlannedStartDate() + ")");
            }
            if (project.getPlannedEndDate() != null && plannedEnd != null && plannedEnd.isAfter(project.getPlannedEndDate())) {
                throw new BadRequestException("Work Package planned end date (" + plannedEnd +
                        ") cannot be later than Project planned end date (" + project.getPlannedEndDate() + ")");
            }
        }
    }

    private void validateAllActivitiesCompleted(Long workPackageId) {
        List<WbsActivity> activities = wbsActivityRepository.findByWorkPackageIdOrderBySequenceOrderAsc(workPackageId);
        long incompleteCount = activities.stream()
                .filter(a -> !"COMPLETED".equalsIgnoreCase(a.getStatus()))
                .count();

        if (incompleteCount > 0) {
            throw new BadRequestException("Cannot set Work Package status to COMPLETED: " + incompleteCount +
                    " activity/activities under this package are not yet COMPLETED.");
        }
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

        List<WbsActivity> rootActivities = wbsActivityRepository.findByWorkPackageIdAndParentIsNullOrderBySequenceOrderAsc(wp.getId());
        List<WbsActivity> allActivities = wbsActivityRepository.findByWorkPackageIdOrderBySequenceOrderAsc(wp.getId());

        int totalCount = allActivities.size();
        int completedCount = (int) allActivities.stream()
                .filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus()))
                .count();

        double totalWeight = rootActivities.stream().mapToDouble(a -> a.getWeightage() != null ? a.getWeightage() : 1.0).sum();
        double weightedSum = rootActivities.stream()
                .mapToDouble(a -> (a.getProgressPercentage() != null ? a.getProgressPercentage() : 0.0) * (a.getWeightage() != null ? a.getWeightage() : 1.0))
                .sum();
        double wpProgress = totalWeight > 0 ? (weightedSum / totalWeight) : 0.0;
        wpProgress = BigDecimal.valueOf(wpProgress).setScale(2, RoundingMode.HALF_UP).doubleValue();

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
                .totalActivities(totalCount)
                .completedActivities(completedCount)
                .progressPercentage(wpProgress)
                .createdAt(wp.getCreatedAt())
                .updatedAt(wp.getUpdatedAt())
                .build();
    }
}
