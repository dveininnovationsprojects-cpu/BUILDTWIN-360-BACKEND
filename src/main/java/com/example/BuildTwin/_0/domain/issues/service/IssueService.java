package com.example.BuildTwin._0.domain.issues.service;

import com.example.BuildTwin._0.domain.issues.dto.IssueResolutionDto;
import com.example.BuildTwin._0.domain.issues.dto.IssueSummaryDto;
import com.example.BuildTwin._0.domain.issues.dto.RiskStatusUpdateDto;
import com.example.BuildTwin._0.domain.issues.dto.RiskSummaryDto;
import com.example.BuildTwin._0.domain.issues.model.IssueBlocker;
import com.example.BuildTwin._0.domain.issues.model.ProjectRisk;

import java.util.List;

public interface IssueService {

    // Issue & Blocker Operations
    IssueBlocker createIssue(IssueBlocker issue);
    IssueBlocker updateIssue(Long id, IssueBlocker issue);
    IssueBlocker getIssueById(Long id);
    List<IssueBlocker> getIssuesByProject(Long projectId);
    List<IssueBlocker> getIssuesByProject(Long projectId, String issueType, String status, String severity);
    List<IssueBlocker> getBlockersByProject(Long projectId);
    IssueBlocker resolveIssue(Long id);
    IssueBlocker resolveIssue(Long id, IssueResolutionDto resolutionDto);
    IssueBlocker updateStatus(Long id, String status);
    void deleteIssue(Long id);
    IssueSummaryDto getIssueSummary(Long projectId);

    // Risk Register Operations
    ProjectRisk createRisk(ProjectRisk risk);
    ProjectRisk updateRisk(Long id, ProjectRisk risk);
    ProjectRisk updateRiskStatus(Long id, RiskStatusUpdateDto statusDto);
    ProjectRisk getRiskById(Long id);
    List<ProjectRisk> getRisksByProject(Long projectId);
    List<ProjectRisk> getRisksByProject(Long projectId, String status, String severity);
    void deleteRisk(Long id);
    RiskSummaryDto getRiskSummary(Long projectId);
}
