package com.example.BuildTwin._0.domain.issues.service;

import com.example.BuildTwin._0.domain.issues.model.*;

import java.util.List;

public interface IssueService {
    IssueBlocker createIssue(IssueBlocker issue);
    List<IssueBlocker> getIssuesByProject(Long projectId);
    IssueBlocker resolveIssue(Long id);
    ProjectRisk createRisk(ProjectRisk risk);
    List<ProjectRisk> getRisksByProject(Long projectId);
}
