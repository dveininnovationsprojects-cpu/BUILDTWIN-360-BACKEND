package com.example.BuildTwin._0.domain.quality.service;

import com.example.BuildTwin._0.domain.quality.dto.QualityResolutionDto;
import com.example.BuildTwin._0.domain.quality.dto.QualitySummaryDto;
import com.example.BuildTwin._0.domain.quality.dto.QualityVerificationDto;
import com.example.BuildTwin._0.domain.quality.model.QualityEvidence;
import com.example.BuildTwin._0.domain.quality.model.QualityIssue;
import com.example.BuildTwin._0.domain.quality.repository.QualityEvidenceRepository;
import com.example.BuildTwin._0.domain.quality.repository.QualityIssueRepository;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QualityServiceImpl implements QualityService {

    private final QualityIssueRepository qualityIssueRepository;
    private final QualityEvidenceRepository qualityEvidenceRepository;

    @Override
    public QualityIssue createQualityIssue(QualityIssue issue) {
        if (issue.getStatus() == null || issue.getStatus().isBlank()) {
            issue.setStatus("OPEN");
        }
        if (issue.getSeverity() == null || issue.getSeverity().isBlank()) {
            issue.setSeverity("MEDIUM");
        }
        if (issue.getIssueType() == null || issue.getIssueType().isBlank()) {
            issue.setIssueType("SNAG");
        }
        if ("NCR".equalsIgnoreCase(issue.getIssueType()) && (issue.getNcrNumber() == null || issue.getNcrNumber().isBlank())) {
            issue.setNcrNumber("NCR-PRJ" + issue.getProjectId() + "-" + System.currentTimeMillis() % 100000);
        }
        log.info("Creating Quality Issue/NCR: {} for Project: {}", issue.getTitle(), issue.getProjectId());
        return qualityIssueRepository.save(issue);
    }

    @Override
    public QualityIssue updateQualityIssue(Long id, QualityIssue updated) {
        QualityIssue existing = getQualityIssueById(id);
        if (updated.getTitle() != null) existing.setTitle(updated.getTitle());
        if (updated.getCategory() != null) existing.setCategory(updated.getCategory());
        if (updated.getLocation() != null) existing.setLocation(updated.getLocation());
        if (updated.getSeverity() != null) existing.setSeverity(updated.getSeverity());
        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
        if (updated.getResponsibleParty() != null) existing.setResponsibleParty(updated.getResponsibleParty());
        if (updated.getDueDate() != null) existing.setDueDate(updated.getDueDate());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getRectificationPlan() != null) existing.setRectificationPlan(updated.getRectificationPlan());
        if (updated.getIssueType() != null) existing.setIssueType(updated.getIssueType());
        if (updated.getNcrNumber() != null) existing.setNcrNumber(updated.getNcrNumber());
        return qualityIssueRepository.save(existing);
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
    @Transactional(readOnly = true)
    public List<QualityIssue> getQualityIssuesByProject(Long projectId, String status, String severity, String issueType) {
        List<QualityIssue> list = qualityIssueRepository.findByProjectId(projectId);
        return list.stream()
                .filter(i -> status == null || status.isBlank() || i.getStatus().equalsIgnoreCase(status))
                .filter(i -> severity == null || severity.isBlank() || i.getSeverity().equalsIgnoreCase(severity))
                .filter(i -> issueType == null || issueType.isBlank() || (i.getIssueType() != null && i.getIssueType().equalsIgnoreCase(issueType)))
                .collect(Collectors.toList());
    }

    @Override
    public QualityIssue updateStatus(Long id, String status) {
        QualityIssue issue = getQualityIssueById(id);
        issue.setStatus(status.toUpperCase());
        log.info("Transitioned Quality Issue ID: {} to status: {}", id, status);
        return qualityIssueRepository.save(issue);
    }

    @Override
    public QualityIssue resolveQualityIssue(Long id, QualityResolutionDto resolutionDto) {
        QualityIssue issue = getQualityIssueById(id);
        issue.setResolutionNotes(resolutionDto.getResolutionNotes());
        issue.setResolvedBy(resolutionDto.getResolvedBy());
        issue.setResolvedAt(LocalDateTime.now());
        issue.setStatus("RECTIFICATION_SUBMITTED");
        log.info("Submitted Rectification Resolution for Quality Issue ID: {} by {}", id, resolutionDto.getResolvedBy());
        return qualityIssueRepository.save(issue);
    }

    @Override
    public QualityIssue verifyQualityIssue(Long id, QualityVerificationDto verificationDto) {
        QualityIssue issue = getQualityIssueById(id);
        issue.setVerifiedBy(verificationDto.getVerifiedBy());
        issue.setVerifiedAt(LocalDateTime.now());
        if (Boolean.TRUE.equals(verificationDto.getApproved())) {
            issue.setStatus("CLOSED");
        } else {
            issue.setStatus("REOPENED");
            if (verificationDto.getRemarks() != null) {
                issue.setDescription((issue.getDescription() == null ? "" : issue.getDescription() + "\n") 
                        + "[Verification Rejection Remarks]: " + verificationDto.getRemarks());
            }
        }
        log.info("Verified Quality Issue ID: {} by {} | Approved: {}", id, verificationDto.getVerifiedBy(), verificationDto.getApproved());
        return qualityIssueRepository.save(issue);
    }

    @Override
    public void deleteQualityIssue(Long id) {
        QualityIssue issue = getQualityIssueById(id);
        qualityIssueRepository.delete(issue);
        log.info("Deleted Quality Issue ID: {}", id);
    }

    @Override
    public QualityEvidence addEvidence(QualityEvidence evidence) {
        log.info("Attaching quality evidence for issue ID: {} at stage: {}", evidence.getQualityIssueId(), evidence.getStage());
        return qualityEvidenceRepository.save(evidence);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QualityEvidence> getEvidenceByQualityIssueId(Long qualityIssueId) {
        return qualityEvidenceRepository.findByQualityIssueId(qualityIssueId);
    }

    @Override
    @Transactional(readOnly = true)
    public QualitySummaryDto getQualitySummary(Long projectId) {
        List<QualityIssue> issues = qualityIssueRepository.findByProjectId(projectId);

        long total = issues.size();
        long openSnags = issues.stream()
                .filter(i -> !"CLOSED".equalsIgnoreCase(i.getStatus()) && !"VERIFIED".equalsIgnoreCase(i.getStatus()))
                .filter(i -> "SNAG".equalsIgnoreCase(i.getIssueType()) || i.getIssueType() == null)
                .count();
        long openNcrs = issues.stream()
                .filter(i -> !"CLOSED".equalsIgnoreCase(i.getStatus()) && !"VERIFIED".equalsIgnoreCase(i.getStatus()))
                .filter(i -> "NCR".equalsIgnoreCase(i.getIssueType()))
                .count();
        long pendingVerification = issues.stream()
                .filter(i -> "RECTIFICATION_SUBMITTED".equalsIgnoreCase(i.getStatus()))
                .count();
        long closed = issues.stream()
                .filter(i -> "CLOSED".equalsIgnoreCase(i.getStatus()) || "VERIFIED".equalsIgnoreCase(i.getStatus()))
                .count();
        long critical = issues.stream()
                .filter(i -> !"CLOSED".equalsIgnoreCase(i.getStatus()))
                .filter(i -> "CRITICAL".equalsIgnoreCase(i.getSeverity()))
                .count();
        long high = issues.stream()
                .filter(i -> !"CLOSED".equalsIgnoreCase(i.getStatus()))
                .filter(i -> "HIGH".equalsIgnoreCase(i.getSeverity()))
                .count();

        return QualitySummaryDto.builder()
                .projectId(projectId)
                .totalIssues(total)
                .openSnags(openSnags)
                .openNcrs(openNcrs)
                .pendingVerification(pendingVerification)
                .closedCount(closed)
                .criticalCount(critical)
                .highCount(high)
                .build();
    }
}
