package com.example.BuildTwin._0.domain.issues.service;

import com.example.BuildTwin._0.domain.issues.model.*;

import java.util.List;

public interface IssueService {
    IssueBlocker createIssue(IssueBlocker issue);
    IssueBlocker getIssueById(Long id);
    List<IssueBlocker> getIssuesByProject(Long projectId);
    IssueBlocker resolveIssue(Long id);
    void deleteIssue(Long id);
    ProjectRisk createRisk(ProjectRisk risk);
    ProjectRisk getRiskById(Long id);
    List<ProjectRisk> getRisksByProject(Long projectId);
    void deleteRisk(Long id);
}
