package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.dependency.*;

import java.util.List;

public interface ActivityDependencyService {

    /**
     * Create a new precedence dependency between two activities in the same project.
     * Validates against circular dependencies (cycles/loops) and duplicates.
     */
    ActivityDependencyResponse createDependency(CreateActivityDependencyRequest request, String performedBy);

    /**
     * Bulk create multiple activity dependencies in batch.
     */
    List<ActivityDependencyResponse> createBatchDependencies(BatchCreateActivityDependencyRequest request, String performedBy);

    /**
     * Retrieve a specific dependency by its primary ID.
     */
    ActivityDependencyResponse getDependencyById(Long id);

    /**
     * Update dependency properties (dependency type, lag days, remarks).
     */
    ActivityDependencyResponse updateDependency(Long id, UpdateActivityDependencyRequest request, String performedBy);

    /**
     * Delete an activity dependency link by ID.
     */
    void deleteDependency(Long id, String performedBy);

    /**
     * Retrieve all activity dependencies defined within a project.
     */
    List<ActivityDependencyResponse> getDependenciesByProjectId(Long projectId);

    /**
     * Retrieve all activity dependencies involving a specific work package.
     */
    List<ActivityDependencyResponse> getDependenciesByWorkPackageId(Long workPackageId);

    /**
     * Retrieve all predecessor activities that the given activity depends on (Incoming dependencies).
     */
    List<ActivityDependencyResponse> getPredecessors(Long activityId);

    /**
     * Retrieve all successor activities that depend on the given activity (Outgoing dependencies).
     */
    List<ActivityDependencyResponse> getSuccessors(Long activityId);

    /**
     * Retrieve complete dependency chain and readiness status (isReadyToStart, blockers list) for an activity.
     */
    ActivityDependencyChainResponse getDependencyChain(Long activityId);

    /**
     * Retrieve the complete CPM project precedence network diagram (nodes & edges) for Gantt chart rendering.
     */
    ProjectDependencyNetworkResponse getProjectDependencyNetwork(Long projectId);

    /**
     * Delete all dependencies (both incoming and outgoing) for an activity.
     */
    void deleteDependenciesByActivityId(Long activityId, String performedBy);
}
