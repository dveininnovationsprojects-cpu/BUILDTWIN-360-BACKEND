package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.integration.*;

public interface ModuleIntegrationService {

    /**
     * Retrieves aggregated module integration status, rollups, and readiness metrics for a project.
     */
    ProjectModuleIntegrationSummaryResponse getProjectModuleIntegrationSummary(Long projectId);

    /**
     * Lists all registered platform modules, their endpoint bases, entity counts, and integration health.
     */
    ProjectModulesListResponse getProjectModulesList(Long projectId);

    /**
     * Retrieves project-wide cross-module entity linkages mapping activities to spatial zones, work packages, and progress.
     */
    ProjectCrossReferenceResponse getProjectCrossReferences(Long projectId);

    /**
     * Deep activity integration view combining spatial hierarchy, WBS, dependencies, baseline schedule, and progress logs.
     */
    ActivityIntegrationDetailsResponse getActivityIntegrationDetails(Long activityId);

    /**
     * System-wide module integration and subsystem operational health check.
     */
    ModuleHealthResponse getSystemIntegrationHealth();
}
