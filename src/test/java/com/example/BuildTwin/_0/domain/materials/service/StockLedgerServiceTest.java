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

    @Test
    @DisplayName("Should process WASTAGE transaction and decrease stock balance")
    void testRecordWastageTransaction() {
        StockTransactionDto dto = StockTransactionDto.builder()
                .projectId(100L)
                .materialId(1L)
                .quantity(new BigDecimal("10.00"))
                .zone("Zone B")
                .contractorId(5L)
                .remarks("Damaged during slab casting")
                .build();

        when(materialRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sampleMaterial));
        when(stockLedgerRepository.save(any(StockLedger.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockLedger result = stockLedgerService.recordWastage(dto);

        assertNotNull(result);
        assertEquals(new BigDecimal("90.00"), sampleMaterial.getCurrentStock());
        assertEquals(StockTransactionType.WASTAGE, result.getTransactionType());
        verify(materialRepository).save(sampleMaterial);
    }

    @Test
    @DisplayName("Should perform stock reconciliation, update physical stock, and record ADJUSTMENT entry")
    void testReconcileStock() {
        com.example.BuildTwin._0.domain.materials.dto.StockReconciliationDto reconDto =
                com.example.BuildTwin._0.domain.materials.dto.StockReconciliationDto.builder()
                        .projectId(100L)
                        .materialId(1L)
                        .physicalQty(new BigDecimal("85.00")) // System stock is 100.00
                        .auditedBy("Auditor_Selvam")
                        .remarks("Physical count 15 bags short due to moisture loss")
                        .build();

        when(materialRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sampleMaterial));
        when(stockLedgerRepository.save(any(StockLedger.class))).thenAnswer(invocation -> {
            StockLedger saved = invocation.getArgument(0);
            return StockLedger.builder()
                    .id(99L)
                    .projectId(saved.getProjectId())
                    .material(saved.getMaterial())
                    .transactionType(saved.getTransactionType())
                    .quantity(saved.getQuantity())
                    .remarks(saved.getRemarks())
                    .build();
        });

        com.example.BuildTwin._0.domain.materials.dto.StockReconciliationResultDto result =
                stockLedgerService.reconcileStock(reconDto);

        assertNotNull(result);
        assertEquals(1L, result.getMaterialId());
        assertEquals(new BigDecimal("100.00"), result.getSystemStock());
        assertEquals(new BigDecimal("85.00"), result.getPhysicalStock());
        assertEquals(new BigDecimal("-15.00"), result.getVariance());
        assertEquals(99L, result.getAdjustmentTransactionId());
        assertEquals(new BigDecimal("85.00"), sampleMaterial.getCurrentStock());
        verify(materialRepository).save(sampleMaterial);
        verify(stockLedgerRepository).save(any(StockLedger.class));
    }

    @Test
    @DisplayName("Should process CONSUMPTION transaction and decrease stock balance")
    void testRecordConsumptionSuccess() {
        StockTransactionDto dto = StockTransactionDto.builder()
                .projectId(100L)
                .materialId(1L)
                .activityId(301L)
                .quantity(new BigDecimal("25.00"))
                .remarks("Consumed in Slab 1 Concreting")
                .build();

        when(materialRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sampleMaterial));
        when(stockLedgerRepository.save(any(StockLedger.class))).thenAnswer(inv -> inv.getArgument(0));

        StockLedger result = stockLedgerService.recordConsumption(dto);

        assertNotNull(result);
        assertEquals(new BigDecimal("75.00"), sampleMaterial.getCurrentStock());
        assertEquals(StockTransactionType.CONSUMPTION, result.getTransactionType());
        verify(materialRepository).save(sampleMaterial);
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when consumption quantity exceeds current stock")
    void testRecordConsumptionInsufficientStock() {
        StockTransactionDto dto = StockTransactionDto.builder()
                .projectId(100L)
                .materialId(1L)
                .activityId(301L)
                .quantity(new BigDecimal("150.00")) // Exceeds 100.00
                .build();

        when(materialRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sampleMaterial));

        assertThrows(InsufficientStockException.class, () -> stockLedgerService.recordConsumption(dto));
        verify(stockLedgerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should process RETURN transaction and increase stock balance")
    void testReturnMaterialSuccess() {
        StockTransactionDto dto = StockTransactionDto.builder()
                .projectId(100L)
                .contractorId(5L)
                .materialId(1L)
                .quantity(new BigDecimal("20.00"))
                .remarks("Contractor returned surplus 20 bags")
                .build();

        when(materialRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sampleMaterial));
        when(stockLedgerRepository.save(any(StockLedger.class))).thenAnswer(inv -> inv.getArgument(0));

        StockLedger result = stockLedgerService.returnMaterial(dto);

        assertNotNull(result);
        assertEquals(new BigDecimal("120.00"), sampleMaterial.getCurrentStock());
        assertEquals(StockTransactionType.RETURN, result.getTransactionType());
        verify(materialRepository).save(sampleMaterial);
        verify(stockLedgerRepository).save(any(StockLedger.class));
    }

    @Test
    @DisplayName("Should record Opening Stock as RECEIPT with remarks and increment stock balance")
    void testOpeningStockReceipt() {
        Material zeroStockMaterial = Material.builder()
                .id(2L)
                .materialCode("MAT-SAND-01")
                .name("River Sand M-Sand")
                .unit(MaterialUnit.CU_M)
                .standardRate(new BigDecimal("1500.00"))
                .currentStock(BigDecimal.ZERO)
                .build();

        StockTransactionDto dto = StockTransactionDto.builder()
                .projectId(100L)
                .materialId(2L)
                .transactionType(StockTransactionType.RECEIPT)
                .quantity(new BigDecimal("50.00"))
                .remarks("Opening Balance")
                .build();

        when(materialRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(zeroStockMaterial));
        when(stockLedgerRepository.save(any(StockLedger.class))).thenAnswer(inv -> inv.getArgument(0));

        StockLedger result = stockLedgerService.recordTransaction(dto);

        assertNotNull(result);
        assertEquals(new BigDecimal("50.00"), zeroStockMaterial.getCurrentStock());
        assertEquals(StockTransactionType.RECEIPT, result.getTransactionType());
        assertEquals("Opening Balance", result.getRemarks());
        verify(materialRepository).save(zeroStockMaterial);
    }

    @Test
    @DisplayName("Should fetch chronological ledger history with project and date range filters")
    void testGetLedgerEntriesByMaterialWithFilters() {
        java.time.LocalDateTime start = java.time.LocalDateTime.now().minusDays(7);
        java.time.LocalDateTime end = java.time.LocalDateTime.now();

        StockLedger entry = StockLedger.builder()
                .id(1L)
                .projectId(100L)
                .material(sampleMaterial)
                .transactionType(StockTransactionType.RECEIPT)
                .quantity(new BigDecimal("100.00"))
                .build();

        when(stockLedgerRepository.findByMaterialIdAndProjectIdAndTimestampBetweenOrderByTimestampAsc(1L, 100L, start, end))
                .thenReturn(java.util.List.of(entry));

        java.util.List<StockLedger> results = stockLedgerService.getLedgerEntriesByMaterial(1L, 100L, start, end);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());
        verify(stockLedgerRepository).findByMaterialIdAndProjectIdAndTimestampBetweenOrderByTimestampAsc(1L, 100L, start, end);
    }
}
