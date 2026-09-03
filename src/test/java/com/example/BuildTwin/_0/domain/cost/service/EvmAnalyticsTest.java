package com.example.BuildTwin._0.domain.cost.service;

import com.example.BuildTwin._0.domain.cost.dto.EvmMetricsDto;
import com.example.BuildTwin._0.domain.cost.repository.BudgetRepository;
import com.example.BuildTwin._0.domain.cost.repository.CostTransactionRepository;
import com.example.BuildTwin._0.domain.materials.repository.StockLedgerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvmAnalyticsTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CostTransactionRepository costTransactionRepository;

    @Mock
    private StockLedgerRepository stockLedgerRepository;

    @InjectMocks
    private CostServiceImpl costService;

    @Test
    @DisplayName("Should correctly calculate all EVM metrics (PV, EV, AC, CV, SV, CPI, SPI, EAC, ETC, VAC)")
    void testCalculateEvmMetrics() {
        Long projectId = 101L;

        // Baseline / Approved budget
        when(budgetRepository.getTotalApprovedBudgetByProjectId(projectId)).thenReturn(new BigDecimal("1000000.00"));
        when(budgetRepository.getTotalBaselineBudgetByProjectId(projectId)).thenReturn(new BigDecimal("1000000.00"));

        // Direct cost & material consumption cost
        when(costTransactionRepository.getTotalActualCostByProjectId(projectId)).thenReturn(new BigDecimal("400000.00"));
        when(stockLedgerRepository.getTotalMaterialConsumptionCostByProjectId(projectId)).thenReturn(new BigDecimal("100000.00"));

        EvmMetricsDto metrics = costService.calculateEvmMetrics(projectId);

        assertNotNull(metrics);
        assertEquals(projectId, metrics.getProjectId());
        assertEquals(new BigDecimal("1000000.00"), metrics.getBudgetAtCompletion());
        assertEquals(new BigDecimal("1000000.00"), metrics.getPlannedValue());
        assertEquals(new BigDecimal("500000.00"), metrics.getActualCost());
        assertEquals(new BigDecimal("500000.00"), metrics.getEarnedValue());
        assertEquals(new BigDecimal("-500000.00"), metrics.getScheduleVariance());
        assertEquals(0, new BigDecimal("0.00").compareTo(metrics.getCostVariance()));
        assertEquals(new BigDecimal("0.5000"), metrics.getSchedulePerformanceIndex());
        assertEquals(new BigDecimal("1.0000"), metrics.getCostPerformanceIndex());
        assertEquals(new BigDecimal("1000000.00"), metrics.getEstimateAtCompletion());
        assertEquals(new BigDecimal("500000.00"), metrics.getEstimateToComplete());
        assertEquals(0, new BigDecimal("0.00").compareTo(metrics.getVarianceAtCompletion()));
        assertEquals("BEHIND_SCHEDULE", metrics.getSpiStatus());
        assertEquals("UNDER_BUDGET", metrics.getCpiStatus());
    }
}
