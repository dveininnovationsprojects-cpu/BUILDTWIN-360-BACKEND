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
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

        // Parent WBS resolution
        WbsActivity parent = null;
        int level = 1;
        if (request.getParentId() != null) {
            parent = wbsActivityRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent WbsActivity", "id", request.getParentId()));

            if (!parent.getWorkPackage().getId().equals(workPackageId)) {
                throw new BadRequestException("Parent Activity ID " + request.getParentId() +
                        " belongs to Work Package ID " + parent.getWorkPackage().getId() +
                        ", not to target Work Package ID " + workPackageId);
            }
            level = (parent.getLevel() != null ? parent.getLevel() : 1) + 1;
        }

        // Location resolution (inherit from parent if not explicitly set)
        Long siteId = request.getSiteId() != null ? request.getSiteId() : (parent != null && parent.getSite() != null ? parent.getSite().getId() : null);
        Long buildingId = request.getBuildingId() != null ? request.getBuildingId() : (parent != null && parent.getBuilding() != null ? parent.getBuilding().getId() : null);
        Long floorId = request.getFloorId() != null ? request.getFloorId() : (parent != null && parent.getFloor() != null ? parent.getFloor().getId() : null);
        Long zoneId = request.getZoneId() != null ? request.getZoneId() : (parent != null && parent.getZone() != null ? parent.getZone().getId() : null);

        Site site = validateAndResolveSite(siteId, project.getId());
        Building building = validateAndResolveBuilding(buildingId, site);
        Floor floor = validateAndResolveFloor(floorId, building);
        Zone zone = validateAndResolveZone(zoneId, floor);

        if (request.getInchargeUserId() != null && !userRepository.existsById(request.getInchargeUserId())) {
            throw new ResourceNotFoundException("User (Incharge)", "id", request.getInchargeUserId());
        }

        String status = request.getStatus() != null ? validateAndNormalizeStatus(request.getStatus()) : "PLANNED";
        String discipline = request.getDiscipline() != null
                ? validateAndNormalizeDiscipline(request.getDiscipline())
                : (parent != null ? parent.getDiscipline() : workPackage.getDiscipline());

        String contractor = request.getAssignedContractor() != null
                ? request.getAssignedContractor().trim()
                : (parent != null && parent.getAssignedContractor() != null ? parent.getAssignedContractor() : workPackage.getAssignedContractor());

        Long inchargeId = request.getInchargeUserId() != null
                ? request.getInchargeUserId()
                : (parent != null && parent.getInchargeUserId() != null ? parent.getInchargeUserId() : workPackage.getInchargeUserId());

        WbsActivity activity = WbsActivity.builder()
                .project(project)
                .workPackage(workPackage)
                .parent(parent)
                .level(level)
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
                .assignedContractor(contractor)
                .inchargeUserId(inchargeId)
                .weightage(request.getWeightage() != null ? request.getWeightage() : 1.0)
                .sequenceOrder(request.getSequenceOrder() != null ? request.getSequenceOrder() : 1)
                .build();

        WbsActivity saved = wbsActivityRepository.save(activity);

        // Compute wbsPath: e.g. "/1" or "/1/4"
        String path = (parent != null && parent.getWbsPath() != null)
                ? parent.getWbsPath() + "/" + saved.getId()
                : "/" + saved.getId();
        saved.setWbsPath(path);
        saved = wbsActivityRepository.save(saved);

        if (parent != null) {
            rollupParentProgress(parent);
        }

        auditService.logAction(
                performedBy,
                "CREATE_WBS_ACTIVITY",
                "WBS_ACTIVITY",
                String.valueOf(saved.getId()),
                "Created WBS Activity: " + saved.getName() + " (" + saved.getCode() + ") at Level " + saved.getLevel() +
                        (parent != null ? " under Parent: " + parent.getName() : " under package: " + workPackage.getName()),
                null
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public WbsActivityResponse createChildActivity(Long parentActivityId, CreateWbsActivityRequest request, String performedBy) {
        WbsActivity parent = wbsActivityRepository.findById(parentActivityId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent WbsActivity", "id", parentActivityId));

        request.setParentId(parentActivityId);
        return createActivity(parent.getWorkPackage().getId(), request, performedBy);
    }

    @Override
    @Transactional
    public WbsActivityResponse reparentActivity(Long id, ReparentWbsActivityRequest request, String performedBy) {
        WbsActivity act = wbsActivityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", id));

        WbsActivity oldParent = act.getParent();
        WbsActivity newParent = null;

        if (request.getNewParentId() != null) {
            if (act.getId().equals(request.getNewParentId())) {
                throw new BadRequestException("An activity cannot be its own parent (Self-referencing cycle)");
            }

            newParent = wbsActivityRepository.findById(request.getNewParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("New Parent WbsActivity", "id", request.getNewParentId()));

            // Parent must be within the same work package
            if (!newParent.getWorkPackage().getId().equals(act.getWorkPackage().getId())) {
                throw new BadRequestException("Cannot reparent activity to a parent in a different Work Package.");
            }

            // Cycle check: new parent cannot be a descendant of this activity!
            if (newParent.getWbsPath() != null &&
                    (newParent.getWbsPath().equals(act.getWbsPath()) ||
                     newParent.getWbsPath().startsWith(act.getWbsPath() + "/"))) {
                throw new BadRequestException("Cycle detected: Cannot reparent an activity to one of its own descendants.");
            }

            act.setParent(newParent);
            act.setLevel((newParent.getLevel() != null ? newParent.getLevel() : 1) + 1);
            act.setWbsPath(newParent.getWbsPath() + "/" + act.getId());
        } else {
            // Reparent to Root
            act.setParent(null);
            act.setLevel(1);
            act.setWbsPath("/" + act.getId());
        }

        if (request.getNewSequenceOrder() != null) {
            act.setSequenceOrder(request.getNewSequenceOrder());
        }

        WbsActivity saved = wbsActivityRepository.save(act);

        // Update levels and wbsPaths for all descendants recursively
        updateDescendantsPathAndLevel(saved);

        // Re-roll up progress for both old parent and new parent
        if (oldParent != null) {
            rollupParentProgress(oldParent);
        }
        if (newParent != null) {
            rollupParentProgress(newParent);
        }

        auditService.logAction(
                performedBy,
                "REPARENT_WBS_ACTIVITY",
                "WBS_ACTIVITY",
                String.valueOf(saved.getId()),
                "Reparented WBS Activity '" + saved.getName() + "' to " + (newParent != null ? "parent '" + newParent.getName() + "'" : "Root level"),
                null
        );

        return mapToResponse(saved);
    }

    private void updateDescendantsPathAndLevel(WbsActivity parent) {
        List<WbsActivity> children = wbsActivityRepository.findByParentIdOrderBySequenceOrderAsc(parent.getId());
        for (WbsActivity child : children) {
            child.setLevel(parent.getLevel() + 1);
            child.setWbsPath(parent.getWbsPath() + "/" + child.getId());
            wbsActivityRepository.save(child);
            updateDescendantsPathAndLevel(child);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<WbsActivityResponse> getChildActivities(Long parentId) {
        if (!wbsActivityRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("Parent WbsActivity", "id", parentId);
        }
        return wbsActivityRepository.findByParentIdOrderBySequenceOrderAsc(parentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WbsActivityResponse getActivityTree(Long activityId) {
        WbsActivity act = wbsActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", activityId));
        return mapToRecursiveResponse(act);
    }

    private WbsActivityResponse mapToRecursiveResponse(WbsActivity act) {
        WbsActivityResponse res = mapToResponse(act);
        List<WbsActivity> children = wbsActivityRepository.findByParentIdOrderBySequenceOrderAsc(act.getId());
        List<WbsActivityResponse> childResponses = children.stream()
                .map(this::mapToRecursiveResponse)
                .collect(Collectors.toList());
        res.setChildren(childResponses);
        res.setChildCount(children.size());
        res.setHasChildren(!children.isEmpty());
        return res;
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
    public PageResponse<WbsActivityResponse> searchActivities(
            Long projectId, Long workPackageId, Long siteId, Long buildingId, Long floorId, Long zoneId,
            String discipline, String status, String contractor, Long inchargeUserId,
            int page, int size, String sortBy, String sortDir) {

        Sort sort = "desc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<WbsActivity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (projectId != null) {
                predicates.add(cb.equal(root.get("project").get("id"), projectId));
            }
            if (workPackageId != null) {
                predicates.add(cb.equal(root.get("workPackage").get("id"), workPackageId));
            }
            if (siteId != null) {
                predicates.add(cb.equal(root.get("site").get("id"), siteId));
            }
            if (buildingId != null) {
                predicates.add(cb.equal(root.get("building").get("id"), buildingId));
            }
            if (floorId != null) {
                predicates.add(cb.equal(root.get("floor").get("id"), floorId));
            }
            if (zoneId != null) {
                predicates.add(cb.equal(root.get("zone").get("id"), zoneId));
            }
            if (discipline != null && !discipline.trim().isEmpty()) {
                predicates.add(cb.equal(cb.upper(root.get("discipline")), discipline.trim().toUpperCase()));
            }
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(cb.equal(cb.upper(root.get("status")), status.trim().toUpperCase()));
            }
            if (contractor != null && !contractor.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("assignedContractor")), "%" + contractor.trim().toLowerCase() + "%"));
            }
            if (inchargeUserId != null) {
                predicates.add(cb.equal(root.get("inchargeUserId"), inchargeUserId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<WbsActivity> actPage = wbsActivityRepository.findAll(spec, pageable);

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

        if (updated.getParent() != null) {
            rollupParentProgress(updated.getParent());
        }

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

        // Auto roll up progress to parent activity and ancestor chain
        if (updated.getParent() != null) {
            rollupParentProgress(updated.getParent());
        }

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

    private void rollupParentProgress(WbsActivity parent) {
        if (parent == null) return;

        List<WbsActivity> children = wbsActivityRepository.findByParentIdOrderBySequenceOrderAsc(parent.getId());
        if (children.isEmpty()) return;

        double totalWeight = 0.0;
        double weightedProgressSum = 0.0;
        boolean allCompleted = true;
        boolean anyStarted = false;

        for (WbsActivity child : children) {
            double weight = child.getWeightage() != null ? child.getWeightage() : 1.0;
            double prog = child.getProgressPercentage() != null ? child.getProgressPercentage() : 0.0;

            weightedProgressSum += (prog * weight);
            totalWeight += weight;

            if (!"COMPLETED".equalsIgnoreCase(child.getStatus())) {
                allCompleted = false;
            }
            if (prog > 0.0 || "IN_PROGRESS".equalsIgnoreCase(child.getStatus())) {
                anyStarted = true;
            }
        }

        double rolledProgress = totalWeight > 0 ? (weightedProgressSum / totalWeight) : 0.0;
        rolledProgress = BigDecimal.valueOf(rolledProgress).setScale(2, RoundingMode.HALF_UP).doubleValue();
        parent.setProgressPercentage(rolledProgress);

        if (allCompleted || rolledProgress >= 100.0) {
            parent.setStatus("COMPLETED");
            if (parent.getActualEndDate() == null) {
                parent.setActualEndDate(LocalDate.now());
            }
        } else if (anyStarted || rolledProgress > 0.0) {
            parent.setStatus("IN_PROGRESS");
            if (parent.getActualStartDate() == null) {
                parent.setActualStartDate(LocalDate.now());
            }
        }

        WbsActivity savedParent = wbsActivityRepository.save(parent);

        // Recursively roll up to grandparent
        if (savedParent.getParent() != null) {
            rollupParentProgress(savedParent.getParent());
        }
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

        if (updated.getParent() != null) {
            rollupParentProgress(updated.getParent());
        }

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
    public WbsActivityResponse assignActivity(Long id, AssignActivityRequest request, String performedBy) {
        WbsActivity act = wbsActivityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", id));

        if (request.getInchargeUserId() != null && !userRepository.existsById(request.getInchargeUserId())) {
            throw new ResourceNotFoundException("User (Incharge)", "id", request.getInchargeUserId());
        }

        act.setAssignedContractor(request.getAssignedContractor() != null ? request.getAssignedContractor().trim() : null);
        act.setInchargeUserId(request.getInchargeUserId());

        WbsActivity saved = wbsActivityRepository.save(act);

        auditService.logAction(
                performedBy,
                "ASSIGN_ACTIVITY",
                "WBS_ACTIVITY",
                String.valueOf(id),
                "Assigned Activity '" + saved.getName() + "' to Contractor: '" + saved.getAssignedContractor() + "' (Incharge ID: " + saved.getInchargeUserId() + ")",
                null
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public WbsActivityResponse scheduleActivity(Long id, ScheduleActivityRequest request, String performedBy) {
        WbsActivity act = wbsActivityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", id));

        if (request.getPlannedStartDate() != null && request.getPlannedEndDate() != null) {
            if (request.getPlannedEndDate().isBefore(request.getPlannedStartDate())) {
                throw new BadRequestException("Planned end date cannot be before planned start date");
            }
        }
        if (request.getPlannedStartDate() != null) {
            act.setPlannedStartDate(request.getPlannedStartDate());
        }
        if (request.getPlannedEndDate() != null) {
            act.setPlannedEndDate(request.getPlannedEndDate());
        }
        if (request.getActualStartDate() != null) {
            act.setActualStartDate(request.getActualStartDate());
        }
        if (request.getActualEndDate() != null) {
            act.setActualEndDate(request.getActualEndDate());
        }

        WbsActivity saved = wbsActivityRepository.save(act);

        String remarks = request.getReasonForRevision() != null ? " Reason: " + request.getReasonForRevision() : "";
        auditService.logAction(
                performedBy,
                "SCHEDULE_ACTIVITY",
                "WBS_ACTIVITY",
                String.valueOf(id),
                "Updated schedule for Activity '" + saved.getName() + "'." + remarks,
                null
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public WbsActivityResponse relocateActivity(Long id, RelocateActivityRequest request, String performedBy) {
        WbsActivity act = wbsActivityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", id));

        Site site = validateAndResolveSite(request.getSiteId(), act.getProject().getId());
        Building building = validateAndResolveBuilding(request.getBuildingId(), site);
        Floor floor = validateAndResolveFloor(request.getFloorId(), building);
        Zone zone = validateAndResolveZone(request.getZoneId(), floor);

        act.setSite(site);
        act.setBuilding(building);
        act.setFloor(floor);
        act.setZone(zone);

        WbsActivity saved = wbsActivityRepository.save(act);

        auditService.logAction(
                performedBy,
                "RELOCATE_ACTIVITY",
                "WBS_ACTIVITY",
                String.valueOf(id),
                "Relocated Activity '" + saved.getName() + "' to location: " + formatLocation(saved),
                null
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteActivity(Long id, String performedBy) {
        WbsActivity act = wbsActivityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", id));

        WbsActivity parent = act.getParent();
        String name = act.getName();
        wbsActivityRepository.delete(act);

        if (parent != null) {
            rollupParentProgress(parent);
        }

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

        // Overall progress: use root activities to prevent double counting
        List<WbsActivity> rootActivities = activities.stream()
                .filter(a -> a.getParent() == null)
                .collect(Collectors.toList());

        List<WbsActivity> baseForCalculation = rootActivities.isEmpty() ? activities : rootActivities;

        double totalWeight = baseForCalculation.stream().mapToDouble(a -> a.getWeightage() != null ? a.getWeightage() : 1.0).sum();
        double weightedProgressSum = baseForCalculation.stream()
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
            WbsTreeResponse.WorkPackageNode wpNode = buildWorkPackageNode(wp);
            wpNodes.add(wpNode);

            if (wpNode.getProgressPercentage() != null) {
                projectTotalWeightedProgress += wpNode.getProgressPercentage();
                projectTotalWeight += 1.0;
            }
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

    @Override
    @Transactional(readOnly = true)
    public WbsTreeResponse.WorkPackageNode getWorkPackageWbsTree(Long workPackageId) {
        WorkPackage wp = workPackageRepository.findById(workPackageId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkPackage", "id", workPackageId));
        return buildWorkPackageNode(wp);
    }

    private WbsTreeResponse.WorkPackageNode buildWorkPackageNode(WorkPackage wp) {
        List<WbsActivity> rootActivities = wbsActivityRepository.findByWorkPackageIdAndParentIsNullOrderBySequenceOrderAsc(wp.getId());
        List<WbsActivity> allActivities = wbsActivityRepository.findByWorkPackageIdOrderBySequenceOrderAsc(wp.getId());

        List<WbsTreeResponse.ActivityNode> actNodes = rootActivities.stream()
                .map(this::buildRecursiveActivityNode)
                .collect(Collectors.toList());

        int completedCount = (int) allActivities.stream().filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus())).count();

        // Calculate WorkPackage Progress from root activities
        double totalWeight = rootActivities.stream().mapToDouble(a -> a.getWeightage() != null ? a.getWeightage() : 1.0).sum();
        double weightedSum = rootActivities.stream()
                .mapToDouble(a -> (a.getProgressPercentage() != null ? a.getProgressPercentage() : 0.0) * (a.getWeightage() != null ? a.getWeightage() : 1.0))
                .sum();
        double wpProgress = totalWeight > 0 ? (weightedSum / totalWeight) : 0.0;
        wpProgress = BigDecimal.valueOf(wpProgress).setScale(2, RoundingMode.HALF_UP).doubleValue();

        return WbsTreeResponse.WorkPackageNode.builder()
                .id(wp.getId())
                .code(wp.getCode())
                .name(wp.getName())
                .discipline(wp.getDiscipline())
                .status(wp.getStatus())
                .budgetAmount(wp.getBudgetAmount())
                .assignedContractor(wp.getAssignedContractor())
                .progressPercentage(wpProgress)
                .totalActivities(allActivities.size())
                .completedActivities(completedCount)
                .activities(actNodes)
                .build();
    }

    private WbsTreeResponse.ActivityNode buildRecursiveActivityNode(WbsActivity act) {
        List<WbsActivity> children = wbsActivityRepository.findByParentIdOrderBySequenceOrderAsc(act.getId());
        List<WbsTreeResponse.ActivityNode> childNodes = children.stream()
                .map(this::buildRecursiveActivityNode)
                .collect(Collectors.toList());

        return WbsTreeResponse.ActivityNode.builder()
                .id(act.getId())
                .parentId(act.getParent() != null ? act.getParent().getId() : null)
                .level(act.getLevel())
                .wbsPath(act.getWbsPath())
                .hasChildren(!children.isEmpty())
                .code(act.getCode())
                .name(act.getName())
                .uom(act.getUom())
                .plannedQuantity(act.getPlannedQuantity())
                .completedQuantity(act.getCompletedQuantity())
                .progressPercentage(act.getProgressPercentage())
                .status(act.getStatus())
                .location(formatLocation(act))
                .sequenceOrder(act.getSequenceOrder())
                .children(childNodes)
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

        long childCount = wbsActivityRepository.countByParentId(act.getId());

        return WbsActivityResponse.builder()
                .id(act.getId())
                .parentId(act.getParent() != null ? act.getParent().getId() : null)
                .parentCode(act.getParent() != null ? act.getParent().getCode() : null)
                .parentName(act.getParent() != null ? act.getParent().getName() : null)
                .level(act.getLevel() != null ? act.getLevel() : 1)
                .wbsPath(act.getWbsPath())
                .hasChildren(childCount > 0)
                .childCount((int) childCount)
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
