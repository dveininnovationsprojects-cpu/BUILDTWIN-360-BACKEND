package com.example.BuildTwin._0.domain.issues.service;

import com.example.BuildTwin._0.domain.issues.model.*;
import com.example.BuildTwin._0.domain.issues.repository.*;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IssueServiceImpl implements IssueService {

    private final IssueBlockerRepository issueBlockerRepository;
    private final ProjectRiskRepository projectRiskRepository;

    @Override
    public IssueBlocker createIssue(IssueBlocker issue) {
        if (issue.getStatus() == null) {
            issue.setStatus("OPEN");
        }
        return issueBlockerRepository.save(issue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueBlocker> getIssuesByProject(Long projectId) {
        return issueBlockerRepository.findByProjectId(projectId);
    }

    @Override
    public IssueBlocker resolveIssue(Long id) {
        IssueBlocker issue = issueBlockerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with ID: " + id));
        issue.setStatus("RESOLVED");
        return issueBlockerRepository.save(issue);
    }

    @Override
    public ProjectRisk createRisk(ProjectRisk risk) {
        if (risk.getStatus() == null) {
            risk.setStatus("IDENTIFIED");
        }
        return projectRiskRepository.save(risk);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectRisk> getRisksByProject(Long projectId) {
        return projectRiskRepository.findByProjectId(projectId);
    }
}
