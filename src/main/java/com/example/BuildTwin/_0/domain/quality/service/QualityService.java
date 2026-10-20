package com.example.BuildTwin._0.domain.quality.service;

import com.example.BuildTwin._0.domain.quality.dto.QualityResolutionDto;
import com.example.BuildTwin._0.domain.quality.dto.QualitySummaryDto;
import com.example.BuildTwin._0.domain.quality.dto.QualityVerificationDto;
import com.example.BuildTwin._0.domain.quality.model.QualityEvidence;
import com.example.BuildTwin._0.domain.quality.model.QualityIssue;

import java.util.List;

public interface QualityService {
    QualityIssue createQualityIssue(QualityIssue issue);
    QualityIssue updateQualityIssue(Long id, QualityIssue updatedIssue);
    QualityIssue getQualityIssueById(Long id);
    List<QualityIssue> getAllQualityIssues();
    List<QualityIssue> getAllQualityIssues(String status, String severity, String issueType);
    List<QualityIssue> getQualityIssuesByProject(Long projectId);
    List<QualityIssue> getQualityIssuesByProject(Long projectId, String status, String severity, String issueType);
    QualityIssue updateStatus(Long id, String status);
    QualityIssue resolveQualityIssue(Long id, QualityResolutionDto resolutionDto);
    QualityIssue verifyQualityIssue(Long id, QualityVerificationDto verificationDto);
    void deleteQualityIssue(Long id);
    QualityEvidence addEvidence(QualityEvidence evidence);
    List<QualityEvidence> getEvidenceByQualityIssueId(Long qualityIssueId);
    QualitySummaryDto getQualitySummary(Long projectId);
    List<QualityIssue> getOverdueIssuesByProject(Long projectId);
}
