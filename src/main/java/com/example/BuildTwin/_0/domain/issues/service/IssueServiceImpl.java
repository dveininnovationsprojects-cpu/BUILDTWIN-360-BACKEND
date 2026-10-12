package com.example.BuildTwin._0.domain.issues.service;

import com.example.BuildTwin._0.domain.issues.dto.IssueResolutionDto;
import com.example.BuildTwin._0.domain.issues.dto.IssueSummaryDto;
import com.example.BuildTwin._0.domain.issues.dto.RiskStatusUpdateDto;
import com.example.BuildTwin._0.domain.issues.dto.RiskSummaryDto;
import com.example.BuildTwin._0.domain.issues.model.IssueBlocker;
import com.example.BuildTwin._0.domain.issues.model.ProjectRisk;
import com.example.BuildTwin._0.domain.issues.repository.IssueBlockerRepository;
import com.example.BuildTwin._0.domain.issues.repository.ProjectRiskRepository;
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
public class IssueServiceImpl implements IssueService {

    private final IssueBlockerRepository issueBlockerRepository;
    private final ProjectRiskRepository projectRiskRepository;

    // ==========================================
    // Issue & Blocker Management
    // ==========================================

    @Override
    public IssueBlocker createIssue(IssueBlocker issue) {
        if (issue.getStatus() == null || issue.getStatus().isBlank()) {
            issue.setStatus("OPEN");
        }
        if (issue.getSeverity() != null && !"MEDIUM".equalsIgnoreCase(issue.getSeverity())) {
            issue.setPriority(issue.getSeverity());
        } else if (issue.getPriority() != null && !"MEDIUM".equalsIgnoreCase(issue.getPriority())) {
            issue.setSeverity(issue.getPriority());
        } else {
            if (issue.getSeverity() == null || issue.getSeverity().isBlank()) {
                issue.setSeverity("MEDIUM");
            }
            if (issue.getPriority() == null || issue.getPriority().isBlank()) {
                issue.setPriority(issue.getSeverity());
            }
        }
        if (issue.getIssueType() == null || issue.getIssueType().isBlank()) {
            issue.setIssueType("BLOCKER");
        }
        if (issue.getDaysDelayed() == null) {
            issue.setDaysDelayed(0);
        }
        log.info("Creating {} with severity {} and owner {} for Project ID: {}",
                issue.getIssueType(), issue.getSeverity(), issue.getOwner(), issue.getProjectId());
        return issueBlockerRepository.save(issue);
    }

    @Override
    public IssueBlocker updateIssue(Long id, IssueBlocker updated) {
        IssueBlocker existing = getIssueById(id);
        if (updated.getTitle() != null) existing.setTitle(updated.getTitle());
        if (updated.getIssueType() != null) existing.setIssueType(updated.getIssueType());
        if (updated.getSeverity() != null) {
            existing.setSeverity(updated.getSeverity());
            existing.setPriority(updated.getSeverity());
        } else if (updated.getPriority() != null) {
            existing.setPriority(updated.getPriority());
            existing.setSeverity(updated.getPriority());
        }
        if (updated.getOwner() != null) existing.setOwner(updated.getOwner());
        if (updated.getImpactArea() != null) existing.setImpactArea(updated.getImpactArea());
        if (updated.getDaysDelayed() != null) existing.setDaysDelayed(updated.getDaysDelayed());
        if (updated.getDueDate() != null) existing.setDueDate(updated.getDueDate());
        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
        if (updated.getCauseCode() != null) existing.setCauseCode(updated.getCauseCode());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getResolutionNotes() != null) existing.setResolutionNotes(updated.getResolutionNotes());
        return issueBlockerRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public IssueBlocker getIssueById(Long id) {
        return issueBlockerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Issue / Blocker not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueBlocker> getIssuesByProject(Long projectId) {
        return issueBlockerRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueBlocker> getIssuesByProject(Long projectId, String issueType, String status, String severity) {
        List<IssueBlocker> list = issueBlockerRepository.findByProjectId(projectId);
        return list.stream()
                .filter(i -> issueType == null || issueType.isBlank() || (i.getIssueType() != null && i.getIssueType().equalsIgnoreCase(issueType)))
                .filter(i -> status == null || status.isBlank() || i.getStatus().equalsIgnoreCase(status))
                .filter(i -> severity == null || severity.isBlank() || (i.getSeverity() != null && i.getSeverity().equalsIgnoreCase(severity)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueBlocker> getBlockersByProject(Long projectId) {
        List<IssueBlocker> list = issueBlockerRepository.findByProjectId(projectId);
        return list.stream()
                .filter(i -> "BLOCKER".equalsIgnoreCase(i.getIssueType()) || "CRITICAL".equalsIgnoreCase(i.getSeverity()))
                .collect(Collectors.toList());
    }

    @Override
    public IssueBlocker resolveIssue(Long id) {
        return resolveIssue(id, IssueResolutionDto.builder()
                .resolutionNotes("Marked as resolved")
                .resolvedBy("System / Site Engineer")
                .build());
    }

    @Override
    public IssueBlocker resolveIssue(Long id, IssueResolutionDto resolutionDto) {
        IssueBlocker issue = getIssueById(id);
        issue.setStatus("RESOLVED");
        issue.setResolutionNotes(resolutionDto.getResolutionNotes());
        issue.setResolvedBy(resolutionDto.getResolvedBy());
        issue.setResolvedAt(LocalDateTime.now());
        log.info("Resolved Issue / Blocker ID: {} by {} | Notes: {}", id, resolutionDto.getResolvedBy(), resolutionDto.getResolutionNotes());
        return issueBlockerRepository.save(issue);
    }

    @Override
    public IssueBlocker updateStatus(Long id, String status) {
        IssueBlocker issue = getIssueById(id);
        issue.setStatus(status.toUpperCase());
        if ("RESOLVED".equalsIgnoreCase(status) || "CLOSED".equalsIgnoreCase(status)) {
            if (issue.getResolvedAt() == null) {
                issue.setResolvedAt(LocalDateTime.now());
            }
        }
        return issueBlockerRepository.save(issue);
    }

    @Override
    public void deleteIssue(Long id) {
        IssueBlocker issue = getIssueById(id);
        issueBlockerRepository.delete(issue);
        log.info("Deleted Issue / Blocker ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public IssueSummaryDto getIssueSummary(Long projectId) {
        List<IssueBlocker> issues = issueBlockerRepository.findByProjectId(projectId);

        long total = issues.size();
        long activeBlockers = issues.stream()
                .filter(i -> !"RESOLVED".equalsIgnoreCase(i.getStatus()) && !"CLOSED".equalsIgnoreCase(i.getStatus()))
                .filter(i -> "BLOCKER".equalsIgnoreCase(i.getIssueType()) || "CRITICAL".equalsIgnoreCase(i.getSeverity()))
                .count();
        long openIssues = issues.stream()
                .filter(i -> "OPEN".equalsIgnoreCase(i.getStatus()))
                .count();
        long inProgress = issues.stream()
                .filter(i -> "IN_PROGRESS".equalsIgnoreCase(i.getStatus()))
                .count();
        long resolved = issues.stream()
                .filter(i -> "RESOLVED".equalsIgnoreCase(i.getStatus()) || "CLOSED".equalsIgnoreCase(i.getStatus()))
                .count();
        long critical = issues.stream()
                .filter(i -> !"RESOLVED".equalsIgnoreCase(i.getStatus()) && !"CLOSED".equalsIgnoreCase(i.getStatus()))
                .filter(i -> "CRITICAL".equalsIgnoreCase(i.getSeverity()))
                .count();
        long high = issues.stream()
                .filter(i -> !"RESOLVED".equalsIgnoreCase(i.getStatus()) && !"CLOSED".equalsIgnoreCase(i.getStatus()))
                .filter(i -> "HIGH".equalsIgnoreCase(i.getSeverity()))
                .count();
        int totalDaysDelayed = issues.stream()
                .filter(i -> !"RESOLVED".equalsIgnoreCase(i.getStatus()) && !"CLOSED".equalsIgnoreCase(i.getStatus()))
                .mapToInt(i -> i.getDaysDelayed() != null ? i.getDaysDelayed() : 0)
                .sum();

        return IssueSummaryDto.builder()
                .projectId(projectId)
                .totalIssues(total)
                .activeBlockers(activeBlockers)
                .openIssues(openIssues)
                .inProgressCount(inProgress)
                .resolvedCount(resolved)
                .criticalCount(critical)
                .highCount(high)
                .totalDaysDelayed(totalDaysDelayed)
                .build();
    }

    // ==========================================
    // Risk Register Management
    // ==========================================

    @Override
    public ProjectRisk createRisk(ProjectRisk risk) {
        if (risk.getStatus() == null || risk.getStatus().isBlank()) {
            risk.setStatus("IDENTIFIED");
        }
        risk.computeSeverityAndScore();
        log.info("Registered Project Risk: {} | Prob: {}, Impact: {}, Severity: {}, Score: {}",
                risk.getRiskTitle(), risk.getProbability(), risk.getImpact(), risk.getSeverity(), risk.getRiskScore());
        return projectRiskRepository.save(risk);
    }

    @Override
    public ProjectRisk updateRisk(Long id, ProjectRisk updated) {
        ProjectRisk existing = getRiskById(id);
        if (updated.getRiskTitle() != null) existing.setRiskTitle(updated.getRiskTitle());
        if (updated.getProbability() != null) existing.setProbability(updated.getProbability());
        if (updated.getImpact() != null) existing.setImpact(updated.getImpact());
        if (updated.getCategory() != null) existing.setCategory(updated.getCategory());
        if (updated.getMitigation() != null) existing.setMitigation(updated.getMitigation());
        if (updated.getContingencyPlan() != null) existing.setContingencyPlan(updated.getContingencyPlan());
        if (updated.getOwner() != null) existing.setOwner(updated.getOwner());
        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
        existing.computeSeverityAndScore();
        return projectRiskRepository.save(existing);
    }

    @Override
    public ProjectRisk updateRiskStatus(Long id, RiskStatusUpdateDto statusDto) {
        ProjectRisk risk = getRiskById(id);
        risk.setStatus(statusDto.getStatus().toUpperCase());
        if (statusDto.getMitigation() != null) {
            risk.setMitigation(statusDto.getMitigation());
        }
        if (statusDto.getResolutionNotes() != null) {
            risk.setResolutionNotes(statusDto.getResolutionNotes());
        }
        if (statusDto.getResolvedBy() != null) {
            risk.setResolvedBy(statusDto.getResolvedBy());
        }
        if ("MITIGATED".equalsIgnoreCase(risk.getStatus()) || "CLOSED".equalsIgnoreCase(risk.getStatus())) {
            risk.setResolvedAt(LocalDateTime.now());
        }
        log.info("Updated Risk ID: {} status to {} by {}", id, risk.getStatus(), statusDto.getResolvedBy());
        return projectRiskRepository.save(risk);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectRisk getRiskById(Long id) {
        return projectRiskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectRisk not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectRisk> getRisksByProject(Long projectId) {
        return projectRiskRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectRisk> getRisksByProject(Long projectId, String status, String severity) {
        List<ProjectRisk> list = projectRiskRepository.findByProjectId(projectId);
        return list.stream()
                .filter(r -> status == null || status.isBlank() || r.getStatus().equalsIgnoreCase(status))
                .filter(r -> severity == null || severity.isBlank() || (r.getSeverity() != null && r.getSeverity().equalsIgnoreCase(severity)))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRisk(Long id) {
        ProjectRisk risk = getRiskById(id);
        projectRiskRepository.delete(risk);
        log.info("Deleted Project Risk ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public RiskSummaryDto getRiskSummary(Long projectId) {
        List<ProjectRisk> risks = projectRiskRepository.findByProjectId(projectId);

        long total = risks.size();
        long identified = risks.stream().filter(r -> "IDENTIFIED".equalsIgnoreCase(r.getStatus())).count();
        long monitored = risks.stream().filter(r -> "MONITORED".equalsIgnoreCase(r.getStatus())).count();
        long mitigated = risks.stream().filter(r -> "MITIGATED".equalsIgnoreCase(r.getStatus()) || "CLOSED".equalsIgnoreCase(r.getStatus())).count();

        long critical = risks.stream().filter(r -> "CRITICAL".equalsIgnoreCase(r.getSeverity())).count();
        long high = risks.stream().filter(r -> "HIGH".equalsIgnoreCase(r.getSeverity())).count();
        long medium = risks.stream().filter(r -> "MEDIUM".equalsIgnoreCase(r.getSeverity())).count();
        long low = risks.stream().filter(r -> "LOW".equalsIgnoreCase(r.getSeverity())).count();

        return RiskSummaryDto.builder()
                .projectId(projectId)
                .totalRisks(total)
                .identifiedRisks(identified)
                .monitoredRisks(monitored)
                .mitigatedRisks(mitigated)
                .criticalRisks(critical)
                .highRisks(high)
                .mediumRisks(medium)
                .lowRisks(low)
                .build();
    }
}
