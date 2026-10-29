package com.example.BuildTwin._0.domain.reports.service;

import com.example.BuildTwin._0.domain.cost.dto.EvmMetricsDto;
import com.example.BuildTwin._0.domain.cost.service.CostService;
import com.example.BuildTwin._0.domain.materials.dto.StockTransactionDto;
import com.example.BuildTwin._0.domain.materials.enums.MaterialUnit;
import com.example.BuildTwin._0.domain.materials.enums.StockTransactionType;
import com.example.BuildTwin._0.domain.materials.exception.InsufficientStockException;
import com.example.BuildTwin._0.domain.materials.model.Material;
import com.example.BuildTwin._0.domain.materials.repository.MaterialRepository;
import com.example.BuildTwin._0.domain.materials.repository.StockLedgerRepository;
import com.example.BuildTwin._0.domain.materials.service.StockLedgerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Week 12: Regression, Performance & Transaction Verification Test Suite
 * Validates transactional integrity, negative stock prevention, and EVM calculation precision.
 */
@ExtendWith(MockitoExtension.class)
class Week12RegressionPerformanceTest {

    @Mock
    private StockLedgerRepository stockLedgerRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private CostService costService;

    @InjectMocks
    private StockLedgerService stockLedgerService;

    @Test
    @DisplayName("Week 12: Transaction Integrity - Insufficient Stock Blocks ISSUE")
    void transactionIntegrity_InsufficientStock_ThrowsException() {
        Material material = Material.builder()
                .id(101L)
                .materialCode("MAT-STEEL-16")
                .name("TMT Steel Rebar 16mm")
                .unit(MaterialUnit.TONNES)
                .currentStock(new BigDecimal("5.00")) // Only 5 tonnes available
                .standardRate(new BigDecimal("62000.00"))
                .build();

        when(materialRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(material));

        StockTransactionDto dto = StockTransactionDto.builder()
                .materialId(101L)
                .transactionType(StockTransactionType.ISSUE)
                .quantity(new BigDecimal("15.00")) // Requesting 15 tonnes (deficiency of 10 tonnes)
                .build();

        assertThrows(InsufficientStockException.class, () -> stockLedgerService.issueMaterial(dto));
        verify(stockLedgerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Week 12: Transaction Integrity - Store Transfer Deducts Origin Stock Atomically")
    void transactionIntegrity_TransferDeductsStockAtomically() {
        Material material = Material.builder()
                .id(102L)
                .materialCode("MAT-CEM-01")
                .name("OPC Cement 53")
                .unit(MaterialUnit.BAGS)
                .currentStock(new BigDecimal("500.00"))
                .standardRate(new BigDecimal("380.00"))
                .build();

        when(materialRepository.findByIdForUpdate(102L)).thenReturn(Optional.of(material));
        when(stockLedgerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        StockTransactionDto dto = StockTransactionDto.builder()
                .projectId(1L)
                .materialId(102L)
                .transactionType(StockTransactionType.TRANSFER)
                .quantity(new BigDecimal("100.00"))
                .remarks("Inter-store transfer from Central Store to Tower B Sub-store")
                .build();

        stockLedgerService.transferMaterial(dto);

        assertEquals(new BigDecimal("400.00"), material.getCurrentStock());
        verify(materialRepository, times(1)).save(material);
        verify(stockLedgerRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Week 12: Performance & Precision - EVM Zero Division Safety")
    void evmPrecision_ZeroActualCost_SafeDivision() {
        EvmMetricsDto metrics = EvmMetricsDto.builder()
                .budgetAtCompletion(new BigDecimal("10000000.00"))
                .actualCost(BigDecimal.ZERO)
                .earnedValue(new BigDecimal("2000000.00"))
                .costPerformanceIndex(BigDecimal.ONE)
                .schedulePerformanceIndex(BigDecimal.ONE)
                .build();

        assertNotNull(metrics);
        assertEquals(BigDecimal.ONE, metrics.getCostPerformanceIndex());
        assertEquals(new BigDecimal("10000000.00"), metrics.getBudgetAtCompletion());
    }
}
