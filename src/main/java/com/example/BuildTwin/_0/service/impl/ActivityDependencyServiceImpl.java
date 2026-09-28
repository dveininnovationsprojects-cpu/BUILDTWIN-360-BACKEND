package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.dependency.*;
import com.example.BuildTwin._0.exception.BadRequestException;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.ActivityDependency;
import com.example.BuildTwin._0.model.DependencyType;
import com.example.BuildTwin._0.model.Project;
import com.example.BuildTwin._0.model.WbsActivity;
import com.example.BuildTwin._0.repository.ActivityDependencyRepository;
import com.example.BuildTwin._0.repository.ProjectRepository;
import com.example.BuildTwin._0.repository.WbsActivityRepository;
import com.example.BuildTwin._0.repository.WorkPackageRepository;
import com.example.BuildTwin._0.service.ActivityDependencyService;
import com.example.BuildTwin._0.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityDependencyServiceImpl implements ActivityDependencyService {

    private final ActivityDependencyRepository activityDependencyRepository;
    private final WbsActivityRepository wbsActivityRepository;
    private final ProjectRepository projectRepository;
    private final WorkPackageRepository workPackageRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public ActivityDependencyResponse createDependency(CreateActivityDependencyRequest request, String performedBy) {
        if (request.getPredecessorId() == null || request.getSuccessorId() == null) {
            throw new BadRequestException("Predecessor ID and Successor ID cannot be null");
        }

        Long predId = request.getPredecessorId();
        Long succId = request.getSuccessorId();

        // 1. Self-reference check
        if (predId.equals(succId)) {
            throw new BadRequestException("An activity cannot depend on itself (Self-referencing cycle). Activity ID: " + predId);
        }

        // 2. Fetch predecessor & successor activities
        WbsActivity predecessor = wbsActivityRepository.findById(predId)
                .orElseThrow(() -> new ResourceNotFoundException("Predecessor WbsActivity", "id", predId));

        WbsActivity successor = wbsActivityRepository.findById(succId)
                .orElseThrow(() -> new ResourceNotFoundException("Successor WbsActivity", "id", succId));

        // 3. Cross-project validation
        Long predProjectId = predecessor.getProject().getId();
        Long succProjectId = successor.getProject().getId();
        if (!predProjectId.equals(succProjectId)) {
            throw new BadRequestException("Cross-project dependencies are not permitted. Predecessor belongs to Project ID "
                    + predProjectId + " while Successor belongs to Project ID " + succProjectId);
        }

        // 4. Duplicate dependency check
        if (activityDependencyRepository.existsByPredecessorIdAndSuccessorId(predId, succId)) {
            throw new DuplicateResourceException("ActivityDependency", "predecessor -> successor",
                    predecessor.getCode() + " -> " + successor.getCode());
        }

        // 5. WBS Hierarchy Parent-Child conflict check
        if (predecessor.getWbsPath() != null && successor.getWbsPath() != null) {
            if (successor.getWbsPath().startsWith(predecessor.getWbsPath() + "/") ||
                    predecessor.getWbsPath().startsWith(successor.getWbsPath() + "/")) {
                throw new BadRequestException("Hierarchy conflict: An activity cannot establish a precedence dependency " +
                        "with its own WBS ancestor or descendant.");
            }
        }

        // 6. Directed Cycle Detection (Check if path from Successor to Predecessor already exists)
        validateNoCycleWillBeCreated(predProjectId, succId, predId, predecessor.getName(), successor.getName());

        // 7. Resolve dependency type & lag
        DependencyType depType = DependencyType.fromString(request.getDependencyType());
        int lagDays = request.getLagDays() != null ? request.getLagDays() : 0;

        // 8. Precedence & Activity Status validation
        validateDependencyStatusCompatibility(predecessor, successor, depType);

        ActivityDependency dependency = ActivityDependency.builder()
                .project(predecessor.getProject())
                .predecessor(predecessor)
                .successor(successor)
                .dependencyType(depType)
                .lagDays(lagDays)
                .remarks(request.getRemarks() != null ? request.getRemarks().trim() : null)
                .build();

        ActivityDependency saved = activityDependencyRepository.save(dependency);

        auditService.logAction(
                performedBy,
                "CREATE_ACTIVITY_DEPENDENCY",
                "ACTIVITY_DEPENDENCY",
                String.valueOf(saved.getId()),
                "Linked Predecessor '" + predecessor.getName() + "' (" + predecessor.getCode() +
                        ") -> Successor '" + successor.getName() + "' (" + successor.getCode() +
                        ") [" + depType.name() + (lagDays != 0 ? ", Lag: " + lagDays + "d" : "") + "]",
                null
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public List<ActivityDependencyResponse> createBatchDependencies(BatchCreateActivityDependencyRequest request, String performedBy) {
        if (request.getDependencies() == null || request.getDependencies().isEmpty()) {
            throw new BadRequestException("Dependencies list cannot be empty");
        }

        List<ActivityDependencyResponse> results = new ArrayList<>();
        for (CreateActivityDependencyRequest req : request.getDependencies()) {
            results.add(createDependency(req, performedBy));
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityDependencyResponse getDependencyById(Long id) {
        ActivityDependency dep = activityDependencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ActivityDependency", "id", id));
        return mapToResponse(dep);
    }

    @Override
    @Transactional
    public ActivityDependencyResponse updateDependency(Long id, UpdateActivityDependencyRequest request, String performedBy) {
        ActivityDependency dep = activityDependencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ActivityDependency", "id", id));

        if (request.getDependencyType() != null && !request.getDependencyType().trim().isEmpty()) {
            DependencyType newType = DependencyType.fromString(request.getDependencyType());
            validateDependencyStatusCompatibility(dep.getPredecessor(), dep.getSuccessor(), newType);
            dep.setDependencyType(newType);
        }

        if (request.getLagDays() != null) {
            dep.setLagDays(request.getLagDays());
        }

        if (request.getRemarks() != null) {
            dep.setRemarks(request.getRemarks().trim());
        }

        ActivityDependency updated = activityDependencyRepository.save(dep);

        auditService.logAction(
                performedBy,
                "UPDATE_ACTIVITY_DEPENDENCY",
                "ACTIVITY_DEPENDENCY",
                String.valueOf(updated.getId()),
                "Updated dependency between '" + dep.getPredecessor().getName() + "' and '" +
                        dep.getSuccessor().getName() + "': Type=" + dep.getDependencyType().name() +
                        ", Lag=" + dep.getLagDays() + "d",
                null
        );

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteDependency(Long id, String performedBy) {
        ActivityDependency dep = activityDependencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ActivityDependency", "id", id));

        String predName = dep.getPredecessor().getName();
        String succName = dep.getSuccessor().getName();

        activityDependencyRepository.delete(dep);

        auditService.logAction(
                performedBy,
                "DELETE_ACTIVITY_DEPENDENCY",
                "ACTIVITY_DEPENDENCY",
                String.valueOf(id),
                "Deleted precedence dependency: '" + predName + "' -> '" + succName + "'",
                null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityDependencyResponse> getDependenciesByProjectId(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        return activityDependencyRepository.findByProjectId(projectId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityDependencyResponse> getDependenciesByWorkPackageId(Long workPackageId) {
        if (!workPackageRepository.existsById(workPackageId)) {
            throw new ResourceNotFoundException("WorkPackage", "id", workPackageId);
        }
        return activityDependencyRepository.findByWorkPackageId(workPackageId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityDependencyResponse> getPredecessors(Long activityId) {
        if (!wbsActivityRepository.existsById(activityId)) {
            throw new ResourceNotFoundException("WbsActivity", "id", activityId);
        }
        return activityDependencyRepository.findBySuccessorId(activityId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityDependencyResponse> getSuccessors(Long activityId) {
        if (!wbsActivityRepository.existsById(activityId)) {
            throw new ResourceNotFoundException("WbsActivity", "id", activityId);
        }
        return activityDependencyRepository.findByPredecessorId(activityId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityDependencyChainResponse getDependencyChain(Long activityId) {
        WbsActivity activity = wbsActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("WbsActivity", "id", activityId));

        List<ActivityDependency> incomingDeps = activityDependencyRepository.findBySuccessorId(activityId);
        List<ActivityDependency> outgoingDeps = activityDependencyRepository.findByPredecessorId(activityId);

        List<ActivityDependencyResponse> predResponses = incomingDeps.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        List<ActivityDependencyResponse> succResponses = outgoingDeps.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        // Calculate blockers for FS dependencies
        List<String> blockers = new ArrayList<>();
        for (ActivityDependency dep : incomingDeps) {
            WbsActivity pred = dep.getPredecessor();
            boolean isCompleted = "COMPLETED".equalsIgnoreCase(pred.getStatus()) ||
                    (pred.getProgressPercentage() != null && pred.getProgressPercentage() >= 100.0);

            if (dep.getDependencyType() == DependencyType.FS && !isCompleted) {
                blockers.add(pred.getName() + " (" + pred.getCode() + ") [Status: " + pred.getStatus() +
                        ", Progress: " + (pred.getProgressPercentage() != null ? pred.getProgressPercentage() : 0.0) + "%]");
            }
        }

        boolean isReadyToStart = blockers.isEmpty();

        return ActivityDependencyChainResponse.builder()
                .activityId(activity.getId())
                .activityCode(activity.getCode())
                .activityName(activity.getName())
                .status(activity.getStatus())
                .plannedStartDate(activity.getPlannedStartDate())
                .plannedEndDate(activity.getPlannedEndDate())
                .progressPercentage(activity.getProgressPercentage())
                .isReadyToStart(isReadyToStart)
                .totalPredecessors(predResponses.size())
                .totalSuccessors(succResponses.size())
                .blockingPredecessorsCount(blockers.size())
                .blockingPredecessorNames(blockers)
                .predecessors(predResponses)
                .successors(succResponses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDependencyNetworkResponse getProjectDependencyNetwork(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        List<WbsActivity> activities = wbsActivityRepository.findByProjectIdOrderBySequenceOrderAsc(projectId);
        List<ActivityDependency> dependencies = activityDependencyRepository.findByProjectId(projectId);

        List<ProjectDependencyNetworkResponse.NetworkNode> nodes = activities.stream()
                .map(act -> ProjectDependencyNetworkResponse.NetworkNode.builder()
                        .id(act.getId())
                        .code(act.getCode())
                        .name(act.getName())
                        .discipline(act.getDiscipline())
                        .status(act.getStatus())
                        .workPackageId(act.getWorkPackage() != null ? act.getWorkPackage().getId() : null)
                        .workPackageName(act.getWorkPackage() != null ? act.getWorkPackage().getName() : null)
                        .plannedStartDate(act.getPlannedStartDate())
                        .plannedEndDate(act.getPlannedEndDate())
                        .progressPercentage(act.getProgressPercentage())
                        .level(act.getLevel())
                        .build())
                .collect(Collectors.toList());

        int scheduleConflictCount = 0;
        List<ProjectDependencyNetworkResponse.NetworkEdge> edges = new ArrayList<>();

        for (ActivityDependency dep : dependencies) {
            ActivityDependencyResponse response = mapToResponse(dep);
            if (Boolean.TRUE.equals(response.getHasScheduleConflict())) {
                scheduleConflictCount++;
            }

            edges.add(ProjectDependencyNetworkResponse.NetworkEdge.builder()
                    .id(dep.getId())
                    .fromPredecessorId(dep.getPredecessor().getId())
                    .toSuccessorId(dep.getSuccessor().getId())
                    .dependencyType(dep.getDependencyType().name())
                    .lagDays(dep.getLagDays())
                    .remarks(dep.getRemarks())
                    .hasScheduleConflict(response.getHasScheduleConflict())
                    .build());
        }

        return ProjectDependencyNetworkResponse.builder()
                .projectId(project.getId())
                .projectCode(project.getCode())
                .projectName(project.getName())
                .totalActivities(nodes.size())
                .totalDependencies(edges.size())
                .scheduleConflictsCount(scheduleConflictCount)
                .nodes(nodes)
                .edges(edges)
                .build();
    }

    @Override
    @Transactional
    public void deleteDependenciesByActivityId(Long activityId, String performedBy) {
        if (!wbsActivityRepository.existsById(activityId)) {
            throw new ResourceNotFoundException("WbsActivity", "id", activityId);
        }

        activityDependencyRepository.deleteByActivityId(activityId);

        auditService.logAction(
                performedBy,
                "DELETE_ACTIVITY_DEPENDENCIES_BATCH",
                "ACTIVITY_DEPENDENCY",
                String.valueOf(activityId),
                "Removed all incoming and outgoing precedence dependencies for Activity ID: " + activityId,
                null
        );
    }

    /**
     * Validates status compatibility before linking a dependency.
     */
    private void validateDependencyStatusCompatibility(WbsActivity predecessor, WbsActivity successor, DependencyType depType) {
        if (depType == DependencyType.FS) {
            if ("COMPLETED".equalsIgnoreCase(successor.getStatus()) && !"COMPLETED".equalsIgnoreCase(predecessor.getStatus())) {
                throw new BadRequestException("Cannot establish Finish-to-Start dependency: Successor activity '" +
                        successor.getName() + "' (" + successor.getCode() + ") is already COMPLETED, but predecessor '" +
                        predecessor.getName() + "' (" + predecessor.getCode() + ") is " + predecessor.getStatus() + ".");
            }
            if ("IN_PROGRESS".equalsIgnoreCase(successor.getStatus()) && !"COMPLETED".equalsIgnoreCase(predecessor.getStatus())) {
                throw new BadRequestException("Cannot establish Finish-to-Start dependency: Successor activity '" +
                        successor.getName() + "' (" + successor.getCode() + ") is already IN_PROGRESS, but predecessor '" +
                        predecessor.getName() + "' (" + predecessor.getCode() + ") is not yet COMPLETED (current status: " +
                        predecessor.getStatus() + ").");
            }
        } else if (depType == DependencyType.SS) {
            if (("IN_PROGRESS".equalsIgnoreCase(successor.getStatus()) || "COMPLETED".equalsIgnoreCase(successor.getStatus()))
                    && "PLANNED".equalsIgnoreCase(predecessor.getStatus())) {
                throw new BadRequestException("Cannot establish Start-to-Start dependency: Successor activity '" +
                        successor.getName() + "' (" + successor.getCode() + ") has already started (" +
                        successor.getStatus() + "), but predecessor '" + predecessor.getName() + "' (" +
                        predecessor.getCode() + ") has not yet started.");
            }
        } else if (depType == DependencyType.FF) {
            if ("COMPLETED".equalsIgnoreCase(successor.getStatus()) && !"COMPLETED".equalsIgnoreCase(predecessor.getStatus())) {
                throw new BadRequestException("Cannot establish Finish-to-Finish dependency: Successor activity '" +
                        successor.getName() + "' (" + successor.getCode() + ") is already COMPLETED, but predecessor '" +
                        predecessor.getName() + "' (" + predecessor.getCode() + ") is not yet COMPLETED.");
            }
        }
    }

    /**
     * Graph Cycle Detection using Breadth-First Search (BFS).
     * If 'targetPredId' is already reachable from 'startSuccId', then adding
     * targetPredId -> startSuccId creates a directed cycle (A -> B -> ... -> A).
     */
    private void validateNoCycleWillBeCreated(Long projectId, Long startSuccId, Long targetPredId,
                                              String predName, String succName) {
        List<ActivityDependency> existingDeps = activityDependencyRepository.findByProjectId(projectId);

        // Build adjacency list: node -> outgoing successors
        Map<Long, List<Long>> adjacencyList = new HashMap<>();
        for (ActivityDependency d : existingDeps) {
            adjacencyList.computeIfAbsent(d.getPredecessor().getId(), k -> new ArrayList<>())
                    .add(d.getSuccessor().getId());
        }

        Queue<Long> queue = new LinkedList<>();
        Set<Long> visited = new HashSet<>();

        queue.add(startSuccId);
        visited.add(startSuccId);

        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (current.equals(targetPredId)) {
                throw new BadRequestException("Circular dependency detected! Activity '" + succName + "' (ID: " +
                        startSuccId + ") already leads to Activity '" + predName + "' (ID: " + targetPredId +
                        ") through existing precedence links. Establishing this dependency would create an infinite schedule loop.");
            }

            List<Long> neighbors = adjacencyList.getOrDefault(current, Collections.emptyList());
            for (Long neighbor : neighbors) {
                if (visited.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }
    }

    /**
     * Converts Entity to Response DTO including CPM Schedule Conflict Analysis.
     */
    private ActivityDependencyResponse mapToResponse(ActivityDependency dep) {
        WbsActivity pred = dep.getPredecessor();
        WbsActivity succ = dep.getSuccessor();
        DependencyType type = dep.getDependencyType();
        int lag = dep.getLagDays() != null ? dep.getLagDays() : 0;

        boolean isPredSatisfied = "COMPLETED".equalsIgnoreCase(pred.getStatus()) ||
                (pred.getProgressPercentage() != null && pred.getProgressPercentage() >= 100.0);

        boolean hasConflict = false;
        String severity = "NONE";
        String message = null;

        LocalDate pStart = pred.getPlannedStartDate();
        LocalDate pEnd = pred.getPlannedEndDate();
        LocalDate sStart = succ.getPlannedStartDate();
        LocalDate sEnd = succ.getPlannedEndDate();

        if (type == DependencyType.FS && pEnd != null && sStart != null) {
            LocalDate earliestAllowedStart = pEnd.plusDays(lag);
            if (sStart.isBefore(earliestAllowedStart)) {
                hasConflict = true;
                severity = isPredSatisfied ? "WARNING" : "CRITICAL";
                message = "Predecessor finishes on " + pEnd + " (earliest valid start with " + lag + "d lag is " +
                        earliestAllowedStart + "), but Successor is planned to start early on " + sStart;
            }
        } else if (type == DependencyType.SS && pStart != null && sStart != null) {
            LocalDate earliestAllowedStart = pStart.plusDays(lag);
            if (sStart.isBefore(earliestAllowedStart)) {
                hasConflict = true;
                severity = "WARNING";
                message = "Predecessor starts on " + pStart + " (earliest start with " + lag + "d lag is " +
                        earliestAllowedStart + "), but Successor is planned to start on " + sStart;
            }
        } else if (type == DependencyType.FF && pEnd != null && sEnd != null) {
            LocalDate earliestAllowedEnd = pEnd.plusDays(lag);
            if (sEnd.isBefore(earliestAllowedEnd)) {
                hasConflict = true;
                severity = "WARNING";
                message = "Predecessor finishes on " + pEnd + " (earliest finish with " + lag + "d lag is " +
                        earliestAllowedEnd + "), but Successor finishes on " + sEnd;
            }
        } else if (type == DependencyType.SF && pStart != null && sEnd != null) {
            LocalDate earliestAllowedEnd = pStart.plusDays(lag);
            if (sEnd.isBefore(earliestAllowedEnd)) {
                hasConflict = true;
                severity = "WARNING";
                message = "Predecessor starts on " + pStart + ", but Successor finishes earlier on " + sEnd;
            }
        }

        return ActivityDependencyResponse.builder()
                .id(dep.getId())
                .projectId(dep.getProject().getId())
                .projectName(dep.getProject().getName())
                // Predecessor
                .predecessorId(pred.getId())
                .predecessorCode(pred.getCode())
                .predecessorName(pred.getName())
                .predecessorDiscipline(pred.getDiscipline())
                .predecessorStatus(pred.getStatus())
                .predecessorPlannedStartDate(pred.getPlannedStartDate())
                .predecessorPlannedEndDate(pred.getPlannedEndDate())
                .predecessorActualStartDate(pred.getActualStartDate())
                .predecessorActualEndDate(pred.getActualEndDate())
                .predecessorProgressPercentage(pred.getProgressPercentage())
                .predecessorWorkPackageId(pred.getWorkPackage() != null ? pred.getWorkPackage().getId() : null)
                .predecessorWorkPackageName(pred.getWorkPackage() != null ? pred.getWorkPackage().getName() : null)
                // Successor
                .successorId(succ.getId())
                .successorCode(succ.getCode())
                .successorName(succ.getName())
                .successorDiscipline(succ.getDiscipline())
                .successorStatus(succ.getStatus())
                .successorPlannedStartDate(succ.getPlannedStartDate())
                .successorPlannedEndDate(succ.getPlannedEndDate())
                .successorActualStartDate(succ.getActualStartDate())
                .successorActualEndDate(succ.getActualEndDate())
                .successorProgressPercentage(succ.getProgressPercentage())
                .successorWorkPackageId(succ.getWorkPackage() != null ? succ.getWorkPackage().getId() : null)
                .successorWorkPackageName(succ.getWorkPackage() != null ? succ.getWorkPackage().getName() : null)
                // Config
                .dependencyType(type.name())
                .dependencyTypeName(type.getDisplayName())
                .lagDays(lag)
                .remarks(dep.getRemarks())
                // Conflict
                .hasScheduleConflict(hasConflict)
                .conflictSeverity(severity)
                .conflictMessage(message)
                .isPredecessorSatisfied(isPredSatisfied)
                .createdAt(dep.getCreatedAt())
                .updatedAt(dep.getUpdatedAt())
                .build();
    }
}
