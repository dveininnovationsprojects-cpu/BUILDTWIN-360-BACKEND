package com.example.BuildTwin._0.domain.cost.service;

import com.example.BuildTwin._0.domain.cost.dto.BudgetRequestDto;
import com.example.BuildTwin._0.domain.cost.dto.CostTransactionRequestDto;
import com.example.BuildTwin._0.domain.cost.dto.EvmMetricsDto;
import com.example.BuildTwin._0.domain.cost.enums.CostSourceType;
import com.example.BuildTwin._0.domain.cost.model.Budget;
import com.example.BuildTwin._0.domain.cost.model.CostTransaction;
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
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CostServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CostTransactionRepository costTransactionRepository;

    @Mock
    private StockLedgerRepository stockLedgerRepository;

    @InjectMocks
    private CostServiceImpl costService;

    @Test
    @DisplayName("Should create budget baseline successfully")
    void testCreateBudget() {
        BudgetRequestDto dto = BudgetRequestDto.builder()
                .projectId(100L)
                .costCode("RCC_CIVIL")
                .baselineAmount(new BigDecimal("500000.00"))
                .build();

        when(budgetRepository.findByProjectIdAndCostCode(100L, "RCC_CIVIL")).thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));

        Budget result = costService.createOrUpdateBudget(dto);

        assertNotNull(result);
        assertEquals("RCC_CIVIL", result.getCostCode());
        assertEquals(new BigDecimal("500000.00"), result.getBaselineAmount());
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    @DisplayName("Should record cost transaction successfully")
    void testRecordCostTransaction() {
        CostTransactionRequestDto dto = CostTransactionRequestDto.builder()
                .projectId(100L)
                .costCode("RCC_CIVIL")
                .sourceType(CostSourceType.ACTUAL_EXPENSE)
                .amount(new BigDecimal("120000.00"))
                .transactionDate(LocalDate.now())
                .referenceNumber("INV-2026-001")
                .build();

        when(costTransactionRepository.save(any(CostTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        CostTransaction result = costService.recordCostTransaction(dto);

        assertNotNull(result);
        assertEquals(new BigDecimal("120000.00"), result.getAmount());
        assertEquals(CostSourceType.ACTUAL_EXPENSE, result.getSourceType());
        verify(costTransactionRepository).save(any(CostTransaction.class));
    }

    @Test
    @DisplayName("Should calculate EVM Metrics correctly")
    void testCalculateEvmMetrics() {
        Long projectId = 100L;
        BigDecimal pv = new BigDecimal("500000.00");
        BigDecimal ac = new BigDecimal("200000.00");

        when(budgetRepository.getTotalApprovedBudgetByProjectId(projectId)).thenReturn(pv);
        when(budgetRepository.getTotalBaselineBudgetByProjectId(projectId)).thenReturn(pv);
        when(costTransactionRepository.getTotalActualCostByProjectId(projectId)).thenReturn(ac);
        when(stockLedgerRepository.getTotalMaterialConsumptionCostByProjectId(projectId)).thenReturn(BigDecimal.ZERO);

        EvmMetricsDto evm = costService.calculateEvmMetrics(projectId);

        assertNotNull(evm);
        assertEquals(pv, evm.getPlannedValue());
        assertEquals(ac, evm.getActualCost());
        assertNotNull(evm.getSchedulePerformanceIndex());
        assertNotNull(evm.getCostPerformanceIndex());
    }
}
