package com.example.BuildTwin._0.domain.cost.service;

import com.example.BuildTwin._0.domain.cost.dto.BudgetRequestDto;
import com.example.BuildTwin._0.domain.cost.dto.CostTransactionRequestDto;
import com.example.BuildTwin._0.domain.cost.dto.EvmMetricsDto;
import com.example.BuildTwin._0.domain.cost.model.Budget;
import com.example.BuildTwin._0.domain.cost.model.CostTransaction;
import com.example.BuildTwin._0.domain.cost.repository.BudgetRepository;
import com.example.BuildTwin._0.domain.cost.repository.CostTransactionRepository;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.example.BuildTwin._0.domain.materials.repository.StockLedgerRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class CostServiceImpl implements CostService {

    private final BudgetRepository budgetRepository;
    private final CostTransactionRepository costTransactionRepository;
    private final StockLedgerRepository stockLedgerRepository;

    @Override
    public Budget createOrUpdateBudget(BudgetRequestDto dto) {
        return budgetRepository.findByProjectIdAndCostCode(dto.getProjectId(), dto.getCostCode())
                .map(existing -> {
                    if (dto.getBaselineAmount() != null) {
                        existing.setBaselineAmount(dto.getBaselineAmount());
                    }
                    if (dto.getRevisedAmount() != null) {
                        existing.setRevisedAmount(dto.getRevisedAmount());
                    }
                    if (dto.getActivityId() != null) {
                        existing.setActivityId(dto.getActivityId());
                    }
                    if (dto.getCostHead() != null) {
                        existing.setCostHead(dto.getCostHead());
                    }
                    return budgetRepository.save(existing);
                })
                .orElseGet(() -> budgetRepository.save(Budget.builder()
                        .projectId(dto.getProjectId())
                        .activityId(dto.getActivityId())
                        .costCode(dto.getCostCode())
                        .costHead(dto.getCostHead())
                        .baselineAmount(dto.getBaselineAmount())
                        .revisedAmount(dto.getRevisedAmount() != null ? dto.getRevisedAmount() : BigDecimal.ZERO)
                        .build()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Budget> getBudgetsByProject(Long projectId) {
        return budgetRepository.findByProjectId(projectId);
    }

    @Override
    public CostTransaction recordCostTransaction(CostTransactionRequestDto dto) {
        CostTransaction tx = CostTransaction.builder()
                .projectId(dto.getProjectId())
                .activityId(dto.getActivityId())
                .costCode(dto.getCostCode())
                .costHead(dto.getCostHead())
                .sourceType(dto.getSourceType())
                .amount(dto.getAmount())
                .transactionDate(dto.getTransactionDate())
                .referenceNumber(dto.getReferenceNumber())
                .build();
        return costTransactionRepository.save(tx);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CostTransaction> getCostTransactionsByProject(Long projectId) {
        return costTransactionRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public EvmMetricsDto calculateEvmMetrics(Long projectId) {
        BigDecimal bac = budgetRepository.getTotalApprovedBudgetByProjectId(projectId);
        BigDecimal pv = budgetRepository.getTotalBaselineBudgetByProjectId(projectId);
        
        BigDecimal directCost = costTransactionRepository.getTotalActualCostByProjectId(projectId);
        BigDecimal materialConsumptionCost = stockLedgerRepository.getTotalMaterialConsumptionCostByProjectId(projectId);
        BigDecimal ac = directCost.add(materialConsumptionCost);

        // Earned Value (EV) calculation: defaults to AC when work is on track, or percentage of BAC
        BigDecimal ev = ac.compareTo(BigDecimal.ZERO) > 0 ? ac : pv.multiply(new BigDecimal("0.5"));

        BigDecimal sv = ev.subtract(pv);
        BigDecimal cv = ev.subtract(ac);

        BigDecimal spi = pv.compareTo(BigDecimal.ZERO) > 0 
                ? ev.divide(pv, 4, RoundingMode.HALF_UP) 
                : BigDecimal.ONE;

        BigDecimal cpi = ac.compareTo(BigDecimal.ZERO) > 0 
                ? ev.divide(ac, 4, RoundingMode.HALF_UP) 
                : BigDecimal.ONE;

        BigDecimal eac = cpi.compareTo(BigDecimal.ZERO) > 0
                ? bac.divide(cpi, 2, RoundingMode.HALF_UP)
                : bac;

        BigDecimal etc = eac.subtract(ac).max(BigDecimal.ZERO);
        BigDecimal vac = bac.subtract(eac);

        String spiStatus = spi.compareTo(BigDecimal.ONE) >= 0 ? "ON_SCHEDULE" : "BEHIND_SCHEDULE";
        String cpiStatus = cpi.compareTo(BigDecimal.ONE) >= 0 ? "UNDER_BUDGET" : "OVER_BUDGET";

        return EvmMetricsDto.builder()
                .projectId(projectId)
                .budgetAtCompletion(bac)
                .plannedValue(pv)
                .earnedValue(ev)
                .actualCost(ac)
                .scheduleVariance(sv)
                .costVariance(cv)
                .schedulePerformanceIndex(spi)
                .costPerformanceIndex(cpi)
                .estimateAtCompletion(eac)
                .estimateToComplete(etc)
                .varianceAtCompletion(vac)
                .spiStatus(spiStatus)
                .cpiStatus(cpiStatus)
                .build();
    }
}
