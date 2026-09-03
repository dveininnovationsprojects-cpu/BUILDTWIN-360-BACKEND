package com.example.BuildTwin._0.domain.materials.service;

import com.example.BuildTwin._0.domain.materials.dto.StockTransactionDto;
import com.example.BuildTwin._0.domain.materials.enums.MaterialUnit;
import com.example.BuildTwin._0.domain.materials.enums.StockTransactionType;
import com.example.BuildTwin._0.domain.materials.exception.InsufficientStockException;
import com.example.BuildTwin._0.domain.materials.model.Material;
import com.example.BuildTwin._0.domain.materials.model.StockLedger;
import com.example.BuildTwin._0.domain.materials.repository.MaterialRepository;
import com.example.BuildTwin._0.domain.materials.repository.StockLedgerRepository;
import org.junit.jupiter.api.BeforeEach;
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

@ExtendWith(MockitoExtension.class)
class StockLedgerServiceTest {

    @Mock
    private StockLedgerRepository stockLedgerRepository;

    @Mock
    private MaterialRepository materialRepository;

    @InjectMocks
    private StockLedgerService stockLedgerService;

    private Material sampleMaterial;

    @BeforeEach
    void setUp() {
        sampleMaterial = Material.builder()
                .id(1L)
                .materialCode("MAT-CEM-001")
                .name("Coromandel OPC Cement 50kg")
                .category("CEMENT")
                .unit(MaterialUnit.BAGS)
                .standardRate(new BigDecimal("380.00"))
                .reorderLevel(new BigDecimal("50.00"))
                .currentStock(new BigDecimal("100.00"))
                .build();
    }

    @Test
    @DisplayName("Should process RECEIPT transaction and increase stock balance")
    void testRecordReceiptTransaction() {
        StockTransactionDto dto = StockTransactionDto.builder()
                .projectId(100L)
                .materialId(1L)
                .transactionType(StockTransactionType.RECEIPT)
                .quantity(new BigDecimal("50.00"))
                .unitPrice(new BigDecimal("380.00"))
                .remarks("Batch inward receipt")
                .build();

        when(materialRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sampleMaterial));
        when(stockLedgerRepository.save(any(StockLedger.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockLedger result = stockLedgerService.recordTransaction(dto);

        assertNotNull(result);
        assertEquals(new BigDecimal("150.00"), sampleMaterial.getCurrentStock());
        assertEquals(StockTransactionType.RECEIPT, result.getTransactionType());
        verify(materialRepository).save(sampleMaterial);
        verify(stockLedgerRepository).save(any(StockLedger.class));
    }

    @Test
    @DisplayName("Should process ISSUE transaction and decrease stock balance")
    void testRecordIssueTransactionSuccess() {
        StockTransactionDto dto = StockTransactionDto.builder()
                .projectId(100L)
                .materialId(1L)
                .transactionType(StockTransactionType.ISSUE)
                .quantity(new BigDecimal("40.00"))
                .build();

        when(materialRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sampleMaterial));
        when(stockLedgerRepository.save(any(StockLedger.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockLedger result = stockLedgerService.recordTransaction(dto);

        assertNotNull(result);
        assertEquals(new BigDecimal("60.00"), sampleMaterial.getCurrentStock());
        assertEquals(StockTransactionType.ISSUE, result.getTransactionType());
        verify(materialRepository).save(sampleMaterial);
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when issue quantity exceeds current stock")
    void testRecordIssueTransactionInsufficientStock() {
        StockTransactionDto dto = StockTransactionDto.builder()
                .projectId(100L)
                .materialId(1L)
                .transactionType(StockTransactionType.ISSUE)
                .quantity(new BigDecimal("200.00")) // Exceeds 100.00 stock
                .build();

        when(materialRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sampleMaterial));

        assertThrows(InsufficientStockException.class, () -> stockLedgerService.recordTransaction(dto));
        verify(stockLedgerRepository, never()).save(any());
    }
}
