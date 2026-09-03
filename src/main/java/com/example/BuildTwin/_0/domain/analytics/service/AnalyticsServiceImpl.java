package com.example.BuildTwin._0.domain.analytics.service;

import com.example.BuildTwin._0.domain.analytics.dto.DelayRiskDto;
import com.example.BuildTwin._0.domain.analytics.dto.ProjectHealthDto;
import com.example.BuildTwin._0.domain.cost.dto.EvmMetricsDto;
import com.example.BuildTwin._0.domain.cost.service.CostService;
import com.example.BuildTwin._0.domain.issues.repository.IssueBlockerRepository;
import com.example.BuildTwin._0.domain.materials.repository.MaterialRepository;
import com.example.BuildTwin._0.domain.quality.repository.QualityIssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final IssueBlockerRepository issueBlockerRepository;
    private final QualityIssueRepository qualityIssueRepository;
    private final MaterialRepository materialRepository;
    private final CostService costService;

    @Override
    public ProjectHealthDto calculateProjectHealth(Long projectId) {
        long blockers = issueBlockerRepository.findByProjectId(projectId).stream()
                .filter(i -> !"RESOLVED".equalsIgnoreCase(i.getStatus()))
                .count();

        long qualityIssues = qualityIssueRepository.findByProjectId(projectId).stream()
                .filter(q -> !"CLOSED".equalsIgnoreCase(q.getStatus()))
                .count();

        long lowStock = materialRepository.findLowStockMaterials().size();

        EvmMetricsDto evm = costService.calculateEvmMetrics(projectId);

        // Calculate Composite Health Index
        double deduction = (blockers * 10) + (qualityIssues * 5) + (lowStock * 5);
        if (evm.getSchedulePerformanceIndex() != null && evm.getSchedulePerformanceIndex().doubleValue() < 1.0) {
            deduction += 10.0;
        }
        if (evm.getCostPerformanceIndex() != null && evm.getCostPerformanceIndex().doubleValue() < 1.0) {
            deduction += 10.0;
        }

        double score = Math.max(0.0, 100.0 - deduction);
        BigDecimal healthScore = BigDecimal.valueOf(score);

        String status = score >= 80 ? "HEALTHY" : (score >= 50 ? "AT_RISK" : "CRITICAL");

        return ProjectHealthDto.builder()
                .projectId(projectId)
                .healthScore(healthScore)
                .status(status)
                .scheduleVariance(evm.getScheduleVariance())
                .costVariance(evm.getCostVariance())
                .openBlockersCount(blockers)
                .lowStockAlertsCount(lowStock)
                .openQualityIssuesCount(qualityIssues)
                .build();
    }

    @Override
    public DelayRiskDto calculateDelayRisk(Long projectId) {
        long blockers = issueBlockerRepository.findByProjectId(projectId).stream()
                .filter(i -> !"RESOLVED".equalsIgnoreCase(i.getStatus()))
                .count();

        double riskScore = Math.min(100.0, blockers * 20.0);
        String riskLevel = riskScore >= 75 ? "CRITICAL" : (riskScore >= 50 ? "HIGH" : (riskScore >= 25 ? "MEDIUM" : "LOW"));

        return DelayRiskDto.builder()
                .projectId(projectId)
                .delayRiskScore(BigDecimal.valueOf(riskScore))
                .riskLevel(riskLevel)
                .criticalBlockersCount(blockers)
                .affectedActivitiesCount(blockers)
                .build();
    }
}
