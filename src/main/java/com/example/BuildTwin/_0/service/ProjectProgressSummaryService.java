package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.summary.PortfolioProgressSummaryResponse;
import com.example.BuildTwin._0.dto.summary.ProjectProgressSummaryResponse;
import com.example.BuildTwin._0.dto.summary.WorkPackageProgressSummaryDto;

public interface ProjectProgressSummaryService {

    /**
     * Retrieves the comprehensive progress summary for a specific project.
     *
     * @param projectId Project ID
     * @return Executive progress summary including KPIs, activity breakdown, work package and discipline rollups
     */
    ProjectProgressSummaryResponse getProjectProgressSummary(Long projectId);

    /**
     * Retrieves portfolio-wide progress summary across all registered projects.
     *
     * @return Portfolio-level executive summary and project-by-project breakdown
     */
    PortfolioProgressSummaryResponse getPortfolioProgressSummary();

    /**
     * Retrieves progress summary for a specific work package.
     *
     * @param workPackageId Work package ID
     * @return Work package progress breakdown
     */
    WorkPackageProgressSummaryDto getWorkPackageProgressSummary(Long workPackageId);
}
