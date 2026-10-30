package com.example.BuildTwin._0.domain.issues.service;

import com.example.BuildTwin._0.domain.issues.dto.*;
import com.example.BuildTwin._0.domain.issues.enums.IssueSeverity;
import com.example.BuildTwin._0.domain.issues.enums.IssueStatus;
import com.example.BuildTwin._0.domain.issues.enums.IssueType;

import java.util.List;

public interface ProjectIssueService {

    // Step 1: Identification & Logging (Capture Phase)
    ProjectIssueResponseDto createIssue(CreateProjectIssueDto dto);

    // Step 2: Assignment & SLA Setting (Allocation Phase)
    ProjectIssueResponseDto assignIssue(Long id, AssignProjectIssueDto dto);

    // Step 3: Action & Rectification (Fixing Phase)
    ProjectIssueResponseDto startProgress(Long id);

    ProjectIssueResponseDto resolveIssue(Long id, ResolveProjectIssueDto dto);

    // Step 4: Quality Verification & Closure (Audit Phase)
    // Pass -> CLOSED; Fail -> Reverts to OPEN
    ProjectIssueResponseDto verifyIssue(Long id, VerifyProjectIssueDto dto);

    // Retrieval & Queries
    ProjectIssueResponseDto getIssueById(Long id);

    List<ProjectIssueResponseDto> getIssuesByProject(Long projectId, IssueType type, IssueStatus status, IssueSeverity severity);

    // Step 5: Overdue Escalation & Analytics (Monitoring Phase)
    List<ProjectIssueResponseDto> getOverdueIssuesByProject(Long projectId);

    ProjectIssueSummaryDto getIssueSummaryByProject(Long projectId);

    default ProjectIssueSummaryDto getProjectIssueSummary(Long projectId) {
        return getIssueSummaryByProject(projectId);
    }

    void runDailyOverdueScan();
}
