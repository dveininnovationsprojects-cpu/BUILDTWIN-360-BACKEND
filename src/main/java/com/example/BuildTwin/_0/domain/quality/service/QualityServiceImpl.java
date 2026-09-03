package com.example.BuildTwin._0.domain.quality.service;

import com.example.BuildTwin._0.domain.quality.model.*;
import com.example.BuildTwin._0.domain.quality.repository.*;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class QualityServiceImpl implements QualityService {

    private final QualityIssueRepository qualityIssueRepository;
    private final QualityEvidenceRepository qualityEvidenceRepository;

    @Override
    public QualityIssue createQualityIssue(QualityIssue issue) {
        if (issue.getStatus() == null) {
            issue.setStatus("OPEN");
        }
        if (issue.getSeverity() == null) {
            issue.setSeverity("MEDIUM");
        }
        return qualityIssueRepository.save(issue);
    }

    @Override
    @Transactional(readOnly = true)
    public QualityIssue getQualityIssueById(Long id) {
        return qualityIssueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quality issue not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QualityIssue> getQualityIssuesByProject(Long projectId) {
        return qualityIssueRepository.findByProjectId(projectId);
    }

    @Override
    public QualityIssue updateStatus(Long id, String status) {
        QualityIssue issue = getQualityIssueById(id);
        issue.setStatus(status);
        return qualityIssueRepository.save(issue);
    }

    @Override
    public QualityEvidence addEvidence(QualityEvidence evidence) {
        return qualityEvidenceRepository.save(evidence);
    }
}
