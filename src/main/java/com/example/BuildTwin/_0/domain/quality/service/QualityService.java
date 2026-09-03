package com.example.BuildTwin._0.domain.quality.service;

import com.example.BuildTwin._0.domain.quality.model.*;

import java.util.List;

public interface QualityService {
    QualityIssue createQualityIssue(QualityIssue issue);
    QualityIssue getQualityIssueById(Long id);
    List<QualityIssue> getQualityIssuesByProject(Long projectId);
    QualityIssue updateStatus(Long id, String status);
    QualityEvidence addEvidence(QualityEvidence evidence);
}
