package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.wbs.*;
import com.example.BuildTwin._0.exception.BadRequestException;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.*;
import com.example.BuildTwin._0.repository.*;
import com.example.BuildTwin._0.service.AuditService;
import com.example.BuildTwin._0.service.WbsActivityService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WbsActivityServiceImpl implements WbsActivityService {

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "PLANNED", "IN_PROGRESS", "COMPLETED", "DELAYED", "ON_HOLD"
    );

    private static final Set<String> ALLOWED_DISCIPLINES = Set.of(
            "CIVIL", "STRUCTURAL", "MEP", "ELECTRICAL", "PLUMBING",
            "HVAC", "FINISHING", "FIRE_FIGHTING", "WATERPROOFING", "INFRASTRUCTURE"
    );

    private final WbsActivityRepository wbsActivityRepository;
    private final WorkPackageRepository workPackageRepository;
    private final ProjectRepository projectRepository;
    private final SiteRepository siteRepository;
    private final BuildingRepository buildingRepository;
    private final FloorRepository floorRepository;
    private final ZoneRepository zoneRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public WbsActivityResponse createActivity(Long workPackageId, CreateWbsActivityRequest request, String performedBy) {
        WorkPackage workPackage = workPackageRepository.findById(workPackageId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkPackage", "id", workPackageId));

        Project project = workPackage.getProject();
        String code = request.getCode().trim().toUpperCase();

        if (wbsActivityRepository.existsByWorkPackageIdAndCode(workPackageId, code)) {
            throw new DuplicateResourceException("WbsActivity", "code", code + " in Work Package " + workPackage.getName());
        }

        Site site = validateAndResolveSite(request.getSiteId(), project.getId());
        Building building = validateAndResolveBuilding(request.getBuildingId(), site);
        Floor floor = validateAndResolveFloor(request.getFloorId(), building);
        Zone zone = validateAndResolveZone(request.getZoneId(), floor);

        if (request.getInchargeUserId() != null && !userRepository.existsById(request.getInchargeUserId())) {
            throw new ResourceNotFoundException("User (Incharge)", "id", request.getInchargeUserId());
        }

        String status = request.getStatus() != null ? validateAndNormalizeStatus(request.getStatus()) : "PLANNED";
        String discipline = request.getDiscipline() != null
                ? validateAndNormalizeDiscipline(request.getDiscipline())
                : workPackage.getDiscipline();

        WbsActivity activity = WbsActivity.builder()
                .project(project)
                .workPackage(workPackage)
                .site(site)
                .building(building)
                .floor(floor)
                .zone(zone)
                .code(code)
                .name(request.getName().trim())
                .discipline(discipline)
                .description(request.getDescription())
                .uom(request.getUom().trim().toUpperCase())
                .plannedQuantity(request.getPlannedQuantity())
                .completedQuantity(0.0)
                .progressPercentage(0.0)
                .plannedStartDate(request.getPlannedStartDate())
                .plannedEndDate(request.getPlannedEndDate())
                .status(status)
                .assignedContractor(request.getAssignedContractor() != null
                        ? request.getAssignedContractor().trim()
                        : workPackage.getAssignedContractor())
                .inchargeUserId(request.getInchargeUserId() != null
                        ? request.getInchargeUserId()
                        : workPackage.getInchargeUserId())
                .weightage(request.getWeightage() != null ? request.getWeightage() : 1.0)
                .sequenceOrder(request.getSequenceOrder() != null ? request.getSequenceOrder() : 1)
                .build();

        WbsActivity saved = wbsActivityRepository.save(activity);

        auditService.logAction(
                performedBy,
                "CREATE_WBS_ACTIVITY",
                "WBS_ACTIVITY",
                String.valueOf(saved.getId()),
                "Created WBS Activity: " + saved.getName() + " (" + saved.getCode() + ") under package: " + workPackage.getName(),
                null
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WbsActivityResponse> getActivitiesByWorkPackageId(Long workPackageId) {
        if (!workPackageRepository.existsById(workPackageId)) {
            throw new ResourceNotFoundException("WorkPackage", "id", workPackageId);
        }
        return wbsActivityRepository.findByWorkPackageIdOrderBySequenceOrderAsc(workPackageId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WbsActivityResponse> getActivitiesByWorkPackageIdPaginated(
            Long workPackageId, String status, int page, int size) {
        if (!workPackageRepository.existsById(workPackageId)) {
            throw new ResourceNotFoundException("WorkPackage", "id", workPackageId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("sequenceOrder").ascending());
        Page<WbsActivity> actPage;

        if (status != null && !status.trim().isEmpty()) {
            actPage = wbsActivityRepository.findByWorkPackageIdAndStatus(workPackageId, status.trim().toUpperCase(), pageable);
        } else {
            actPage = wbsActivityRepository.findByWorkPackageId(workPackageId, pageable);
        }

        List<WbsActivityResponse> content = actPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<WbsActivityResponse>builder()
                .content(content)
                .pageNumber(actPage.getNumber())
                .pageSize(actPage.getSize())
                .totalElements(actPage.getTotalElements())
                .totalPages(actPage.getTotalPages())
                .isFirst(actPage.isFirst())
                .isLast(actPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WbsActivityResponse> getActivitiesByProjectId(
            Long projectId, String status, String discipline, int page, int size, String sortBy, String sortDir) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<WbsActivity> actPage;
        if (status != null && !status.trim().isEmpty()) {
            actPage = wbsActivityRepository.findByProjectIdAndStatus(projectId, status.trim().toUpperCase(), pageable);
        } else if (discipline != null && !discipline.trim().isEmpty()) {
            actPage = wbsActivityRepository.findByProjectIdAndDiscipline(projectId, discipline.trim().toUpperCase(), pageable);
        } else {
            actPage = wbsActivityRepository.findByProjectId(projectId, pageable);
        }

        List<WbsActivityResponse> content = actPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<WbsActivityResponse>builder()
                .content(content)
                .pageNumber(actPage.getNumber())
                .pageSize(actPage.getSize())
                .totalElements(actPage.getTotalElements())
                .totalPages(actPage.getTotalPages())
                .isFirst(actPage.isFirst())
                .isLast(actPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WbsActivityResponse getActivityById(Long id) {
        WbsActivity act = wbsActivityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", id));
        return mapToResponse(act);
    }

    @Override
    @Transactional
    public WbsActivityResponse updateActivity(Long id, UpdateWbsActivityRequest request, String performedBy) {
        WbsActivity act = wbsActivityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", id));

        String newCode = request.getCode().trim().toUpperCase();
        if (!act.getCode().equalsIgnoreCase(newCode) &&
                wbsActivityRepository.existsByWorkPackageIdAndCodeAndIdNot(act.getWorkPackage().getId(), newCode, id)) {
            throw new DuplicateResourceException("WbsActivity", "code", newCode + " in Work Package " + act.getWorkPackage().getName());
        }

        Site site = validateAndResolveSite(request.getSiteId(), act.getProject().getId());
        Building building = validateAndResolveBuilding(request.getBuildingId(), site);
        Floor floor = validateAndResolveFloor(request.getFloorId(), building);
        Zone zone = validateAndResolveZone(request.getZoneId(), floor);

        if (request.getInchargeUserId() != null && !userRepository.existsById(request.getInchargeUserId())) {
            throw new ResourceNotFoundException("User (Incharge)", "id", request.getInchargeUserId());
        }

        act.setCode(newCode);
        act.setName(request.getName().trim());
        if (request.getDiscipline() != null) {
            act.setDiscipline(validateAndNormalizeDiscipline(request.getDiscipline()));
        }
        act.setDescription(request.getDescription());
        act.setUom(request.getUom().trim().toUpperCase());
        act.setPlannedQuantity(request.getPlannedQuantity());
        if (request.getCompletedQuantity() != null) {
            act.setCompletedQuantity(request.getCompletedQuantity());
        }
        if (request.getProgressPercentage() != null) {
            act.setProgressPercentage(request.getProgressPercentage());
        }
        act.setSite(site);
        act.setBuilding(building);
        act.setFloor(floor);
        act.setZone(zone);
        act.setPlannedStartDate(request.getPlannedStartDate());
        act.setPlannedEndDate(request.getPlannedEndDate());
        act.setActualStartDate(request.getActualStartDate());
        act.setActualEndDate(request.getActualEndDate());
        if (request.getStatus() != null) {
            act.setStatus(validateAndNormalizeStatus(request.getStatus()));
        }
        act.setAssignedContractor(request.getAssignedContractor());
        act.setInchargeUserId(request.getInchargeUserId());
        if (request.getWeightage() != null) act.setWeightage(request.getWeightage());
        if (request.getSequenceOrder() != null) act.setSequenceOrder(request.getSequenceOrder());

        WbsActivity updated = wbsActivityRepository.save(act);

        auditService.logAction(
                performedBy,
                "UPDATE_WBS_ACTIVITY",
                "WBS_ACTIVITY",
                String.valueOf(updated.getId()),
                "Updated WBS Activity: " + updated.getName() + " (" + updated.getCode() + ")",
                null
        );

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public WbsActivityResponse updateActivityProgress(Long id, UpdateWbsActivityProgressRequest request, String performedBy) {
        WbsActivity act = wbsActivityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", id));

        if (request.getCompletedQuantity() != null) {
            act.setCompletedQuantity(request.getCompletedQuantity());
        }

        double calculatedProgress;
        if (request.getProgressPercentage() != null) {
            calculatedProgress = request.getProgressPercentage();
        } else if (act.getPlannedQuantity() > 0 && act.getCompletedQuantity() != null) {
            calculatedProgress = (act.getCompletedQuantity() / act.getPlannedQuantity()) * 100.0;
            calculatedProgress = Math.min(100.0, Math.max(0.0, calculatedProgress));
        } else {
            calculatedProgress = act.getProgressPercentage();
        }

        // Round to 2 decimal places
        calculatedProgress = BigDecimal.valueOf(calculatedProgress)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
        act.setProgressPercentage(calculatedProgress);

        if (request.getActualStartDate() != null) {
            act.setActualStartDate(request.getActualStartDate());
        } else if (act.getActualStartDate() == null && calculatedProgress > 0.0) {
            act.setActualStartDate(LocalDate.now());
        }

        if (request.getActualEndDate() != null) {
            act.setActualEndDate(request.getActualEndDate());
        } else if (calculatedProgress >= 100.0 && act.getActualEndDate() == null) {
            act.setActualEndDate(LocalDate.now());
        }

        // Auto-update status
        if (calculatedProgress >= 100.0) {
            act.setStatus("COMPLETED");
        } else if (calculatedProgress > 0.0 && "PLANNED".equalsIgnoreCase(act.getStatus())) {
            act.setStatus("IN_PROGRESS");
        }

        WbsActivity updated = wbsActivityRepository.save(act);

        // Auto-update WorkPackage status to IN_PROGRESS if it was PLANNED
        WorkPackage wp = updated.getWorkPackage();
        if ("PLANNED".equalsIgnoreCase(wp.getStatus()) && calculatedProgress > 0.0) {
            wp.setStatus("IN_PROGRESS");
            if (wp.getActualStartDate() == null) {
                wp.setActualStartDate(LocalDate.now());
            }
            workPackageRepository.save(wp);
        }

        auditService.logAction(
                performedBy,
                "UPDATE_ACTIVITY_PROGRESS",
                "WBS_ACTIVITY",
                String.valueOf(updated.getId()),
                "Updated progress for Activity '" + updated.getName() + "' to " + calculatedProgress + "% (Qty: " + updated.getCompletedQuantity() + "/" + updated.getPlannedQuantity() + " " + updated.getUom() + ")",
                null
        );

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public WbsActivityResponse updateActivityStatus(Long id, UpdateWbsActivityStatusRequest request, String performedBy) {
        WbsActivity act = wbsActivityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", id));

        String oldStatus = act.getStatus();
        String newStatus = validateAndNormalizeStatus(request.getStatus());

        act.setStatus(newStatus);
        if ("COMPLETED".equalsIgnoreCase(newStatus) && act.getProgressPercentage() < 100.0) {
            act.setProgressPercentage(100.0);
            act.setCompletedQuantity(act.getPlannedQuantity());
            if (act.getActualEndDate() == null) {
                act.setActualEndDate(LocalDate.now());
            }
        }

        WbsActivity updated = wbsActivityRepository.save(act);

        auditService.logAction(
                performedBy,
                "UPDATE_ACTIVITY_STATUS",
                "WBS_ACTIVITY",
                String.valueOf(id),
                "Changed status of activity '" + updated.getName() + "' from " + oldStatus + " to " + newStatus,
                null
        );

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteActivity(Long id, String performedBy) {
        WbsActivity act = wbsActivityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", id));

        String name = act.getName();
        wbsActivityRepository.delete(act);

        auditService.logAction(
                performedBy,
                "DELETE_WBS_ACTIVITY",
                "WBS_ACTIVITY",
                String.valueOf(id),
                "Deleted WBS Activity: " + name,
                null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public WbsSummaryResponse getWbsSummary(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        List<WorkPackage> workPackages = workPackageRepository.findByProjectId(projectId);
        List<WbsActivity> activities = wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(projectId);

        long activeWp = workPackages.stream().filter(wp -> "IN_PROGRESS".equalsIgnoreCase(wp.getStatus())).count();
        long completedWp = workPackages.stream().filter(wp -> "COMPLETED".equalsIgnoreCase(wp.getStatus())).count();

        long plannedAct = activities.stream().filter(a -> "PLANNED".equalsIgnoreCase(a.getStatus())).count();
        long inProgressAct = activities.stream().filter(a -> "IN_PROGRESS".equalsIgnoreCase(a.getStatus())).count();
        long completedAct = activities.stream().filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus())).count();
        long delayedAct = activities.stream().filter(a -> "DELAYED".equalsIgnoreCase(a.getStatus())).count();

        BigDecimal totalBudget = workPackages.stream()
                .map(wp -> wp.getBudgetAmount() != null ? wp.getBudgetAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Weighted progress percentage
        double totalWeight = activities.stream().mapToDouble(a -> a.getWeightage() != null ? a.getWeightage() : 1.0).sum();
        double weightedProgressSum = activities.stream()
                .mapToDouble(a -> (a.getProgressPercentage() != null ? a.getProgressPercentage() : 0.0) * (a.getWeightage() != null ? a.getWeightage() : 1.0))
                .sum();

        double overallProgress = totalWeight > 0 ? (weightedProgressSum / totalWeight) : 0.0;
        overallProgress = BigDecimal.valueOf(overallProgress).setScale(2, RoundingMode.HALF_UP).doubleValue();

        return WbsSummaryResponse.builder()
                .projectId(project.getId())
                .projectCode(project.getCode())
                .projectName(project.getName())
                .totalWorkPackages((long) workPackages.size())
                .activeWorkPackages(activeWp)
                .completedWorkPackages(completedWp)
                .totalActivities((long) activities.size())
                .plannedActivities(plannedAct)
                .inProgressActivities(inProgressAct)
                .completedActivities(completedAct)
                .delayedActivities(delayedAct)
                .overallProgressPercentage(overallProgress)
                .totalAllocatedBudget(totalBudget)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WbsTreeResponse getWbsTree(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        List<WorkPackage> workPackages = workPackageRepository.findByProjectId(projectId);
        List<WbsTreeResponse.WorkPackageNode> wpNodes = new ArrayList<>();

        double projectTotalWeightedProgress = 0.0;
        double projectTotalWeight = 0.0;

        for (WorkPackage wp : workPackages) {
            List<WbsActivity> activities = wbsActivityRepository.findByWorkPackageIdOrderBySequenceOrderAsc(wp.getId());
            List<WbsTreeResponse.ActivityNode> actNodes = new ArrayList<>();

            double wpWeightedSum = 0.0;
            double wpWeightSum = 0.0;
            int completedCount = 0;

            for (WbsActivity act : activities) {
                double weight = act.getWeightage() != null ? act.getWeightage() : 1.0;
                double prog = act.getProgressPercentage() != null ? act.getProgressPercentage() : 0.0;
                wpWeightedSum += (prog * weight);
                wpWeightSum += weight;

                if ("COMPLETED".equalsIgnoreCase(act.getStatus())) {
                    completedCount++;
                }

                actNodes.add(WbsTreeResponse.ActivityNode.builder()
                        .id(act.getId())
                        .code(act.getCode())
                        .name(act.getName())
                        .uom(act.getUom())
                        .plannedQuantity(act.getPlannedQuantity())
                        .completedQuantity(act.getCompletedQuantity())
                        .progressPercentage(prog)
                        .status(act.getStatus())
                        .location(formatLocation(act))
                        .sequenceOrder(act.getSequenceOrder())
                        .build());
            }

            double wpProgress = wpWeightSum > 0 ? (wpWeightedSum / wpWeightSum) : 0.0;
            wpProgress = BigDecimal.valueOf(wpProgress).setScale(2, RoundingMode.HALF_UP).doubleValue();

            projectTotalWeightedProgress += wpWeightedSum;
            projectTotalWeight += wpWeightSum;

            wpNodes.add(WbsTreeResponse.WorkPackageNode.builder()
                    .id(wp.getId())
                    .code(wp.getCode())
                    .name(wp.getName())
                    .discipline(wp.getDiscipline())
                    .status(wp.getStatus())
                    .budgetAmount(wp.getBudgetAmount())
                    .assignedContractor(wp.getAssignedContractor())
                    .progressPercentage(wpProgress)
                    .totalActivities(activities.size())
                    .completedActivities(completedCount)
                    .activities(actNodes)
                    .build());
        }

        double projectProgress = projectTotalWeight > 0 ? (projectTotalWeightedProgress / projectTotalWeight) : 0.0;
        projectProgress = BigDecimal.valueOf(projectProgress).setScale(2, RoundingMode.HALF_UP).doubleValue();

        return WbsTreeResponse.builder()
                .projectId(project.getId())
                .projectCode(project.getCode())
                .projectName(project.getName())
                .overallProgressPercentage(projectProgress)
                .workPackages(wpNodes)
                .build();
    }

    private String formatLocation(WbsActivity act) {
        StringBuilder sb = new StringBuilder();
        if (act.getSite() != null) sb.append(act.getSite().getName());
        if (act.getBuilding() != null) sb.append(sb.length() > 0 ? " > " : "").append(act.getBuilding().getName());
        if (act.getFloor() != null) sb.append(sb.length() > 0 ? " > " : "").append(act.getFloor().getFloorName());
        if (act.getZone() != null) sb.append(sb.length() > 0 ? " > " : "").append(act.getZone().getName());
        return sb.length() > 0 ? sb.toString() : "Project General Work Front";
    }

    private Site validateAndResolveSite(Long siteId, Long projectId) {
        if (siteId == null) return null;
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site", "id", siteId));
        if (!site.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Site ID " + siteId + " does not belong to Project ID " + projectId);
        }
        return site;
    }

    private Building validateAndResolveBuilding(Long buildingId, Site site) {
        if (buildingId == null) return null;
        Building bld = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", buildingId));
        if (site != null && !bld.getSite().getId().equals(site.getId())) {
            throw new BadRequestException("Building ID " + buildingId + " does not belong to Site ID " + site.getId());
        }
        return bld;
    }

    private Floor validateAndResolveFloor(Long floorId, Building building) {
        if (floorId == null) return null;
        Floor flr = floorRepository.findById(floorId)
                .orElseThrow(() -> new ResourceNotFoundException("Floor", "id", floorId));
        if (building != null && !flr.getBuilding().getId().equals(building.getId())) {
            throw new BadRequestException("Floor ID " + floorId + " does not belong to Building ID " + building.getId());
        }
        return flr;
    }

    private Zone validateAndResolveZone(Long zoneId, Floor floor) {
        if (zoneId == null) return null;
        Zone zn = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", zoneId));
        if (floor != null && !zn.getFloor().getId().equals(floor.getId())) {
            throw new BadRequestException("Zone ID " + zoneId + " does not belong to Floor ID " + floor.getId());
        }
        return zn;
    }

    private String validateAndNormalizeStatus(String status) {
        String normalized = status.trim().toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new BadRequestException("Invalid status: '" + status + "'. Allowed: " + ALLOWED_STATUSES);
        }
        return normalized;
    }

    private String validateAndNormalizeDiscipline(String discipline) {
        String normalized = discipline.trim().toUpperCase();
        if (!ALLOWED_DISCIPLINES.contains(normalized)) {
            throw new BadRequestException("Invalid discipline: '" + discipline + "'. Allowed: " + ALLOWED_DISCIPLINES);
        }
        return normalized;
    }

    private WbsActivityResponse mapToResponse(WbsActivity act) {
        String inchargeName = null;
        if (act.getInchargeUserId() != null) {
            inchargeName = userRepository.findById(act.getInchargeUserId())
                    .map(User::getUsername)
                    .orElse(null);
        }

        return WbsActivityResponse.builder()
                .id(act.getId())
                .workPackageId(act.getWorkPackage().getId())
                .workPackageCode(act.getWorkPackage().getCode())
                .workPackageName(act.getWorkPackage().getName())
                .projectId(act.getProject().getId())
                .projectName(act.getProject().getName())
                .siteId(act.getSite() != null ? act.getSite().getId() : null)
                .siteName(act.getSite() != null ? act.getSite().getName() : null)
                .buildingId(act.getBuilding() != null ? act.getBuilding().getId() : null)
                .buildingName(act.getBuilding() != null ? act.getBuilding().getName() : null)
                .floorId(act.getFloor() != null ? act.getFloor().getId() : null)
                .floorName(act.getFloor() != null ? act.getFloor().getFloorName() : null)
                .zoneId(act.getZone() != null ? act.getZone().getId() : null)
                .zoneName(act.getZone() != null ? act.getZone().getName() : null)
                .code(act.getCode())
                .name(act.getName())
                .discipline(act.getDiscipline())
                .description(act.getDescription())
                .uom(act.getUom())
                .plannedQuantity(act.getPlannedQuantity())
                .completedQuantity(act.getCompletedQuantity())
                .progressPercentage(act.getProgressPercentage())
                .plannedStartDate(act.getPlannedStartDate())
                .plannedEndDate(act.getPlannedEndDate())
                .actualStartDate(act.getActualStartDate())
                .actualEndDate(act.getActualEndDate())
                .status(act.getStatus())
                .assignedContractor(act.getAssignedContractor())
                .inchargeUserId(act.getInchargeUserId())
                .inchargeUserName(inchargeName)
                .weightage(act.getWeightage())
                .sequenceOrder(act.getSequenceOrder())
                .createdAt(act.getCreatedAt())
                .updatedAt(act.getUpdatedAt())
                .build();
    }
}
