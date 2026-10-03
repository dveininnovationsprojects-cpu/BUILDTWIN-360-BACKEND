package com.example.BuildTwin._0.domain.materials.service;

import com.example.BuildTwin._0.domain.materials.dto.MaterialRequestDto;
import com.example.BuildTwin._0.domain.materials.enums.MaterialUnit;
import com.example.BuildTwin._0.domain.materials.model.Material;
import com.example.BuildTwin._0.domain.materials.repository.MaterialRepository;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.domain.materials.dto.MaterialStockBalanceDto;
import com.example.BuildTwin._0.domain.materials.repository.StockLedgerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private StockLedgerRepository stockLedgerRepository;

    @InjectMocks
    private MaterialService materialService;

    @Test
    @DisplayName("Should create material successfully")
    void testCreateMaterialSuccess() {
        MaterialRequestDto dto = MaterialRequestDto.builder()
                .materialCode("MAT-STL-012")
                .name("TMT Steel Bars 12mm")
                .category("STEEL")
                .unit(MaterialUnit.TONNES)
                .standardRate(new BigDecimal("62000.00"))
                .reorderLevel(new BigDecimal("5.00"))
                .build();

        when(materialRepository.existsByMaterialCode("MAT-STL-012")).thenReturn(false);
        when(materialRepository.save(any(Material.class))).thenAnswer(inv -> inv.getArgument(0));

        Material result = materialService.createMaterial(dto);

        assertNotNull(result);
        assertEquals("MAT-STL-012", result.getMaterialCode());
        assertEquals("TMT Steel Bars 12mm", result.getName());
        verify(materialRepository).save(any(Material.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException for duplicate SKU material code")
    void testCreateMaterialDuplicateCode() {
        MaterialRequestDto dto = MaterialRequestDto.builder()
                .materialCode("MAT-STL-012")
                .name("TMT Steel Bars 12mm")
                .unit(MaterialUnit.TONNES)
                .standardRate(new BigDecimal("62000.00"))
                .build();

        when(materialRepository.existsByMaterialCode("MAT-STL-012")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> materialService.createMaterial(dto));
        verify(materialRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return dynamically computed stock balance and status")
    void testGetStockBalance() {
        Material material = Material.builder()
                .id(1L)
                .materialCode("MAT-CEM-01")
                .name("Coromandel Cement")
                .category("CEMENT")
                .unit(MaterialUnit.BAGS)
                .standardRate(new BigDecimal("380.00"))
                .reorderLevel(new BigDecimal("50.00"))
                .currentStock(new BigDecimal("40.00")) // Low stock (40 <= 50)
                .build();

        LocalDateTime lastTxn = LocalDateTime.now().minusHours(2);

        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(stockLedgerRepository.findLatestTimestampByMaterialId(1L)).thenReturn(Optional.of(lastTxn));

        MaterialStockBalanceDto balance = materialService.getStockBalance(1L);

        assertNotNull(balance);
        assertEquals(1L, balance.getMaterialId());
        assertEquals("MAT-CEM-01", balance.getMaterialCode());
        assertEquals(new BigDecimal("40.00"), balance.getCurrentStock());
        assertEquals(new BigDecimal("15200.00"), balance.getTotalStockValue());
        assertTrue(balance.isLowStock());
        assertEquals(lastTxn, balance.getLastTransactionTimestamp());
    }

    @Test
    @DisplayName("Should get all low stock materials when projectId is null")
    void testGetLowStockMaterialsWithoutProject() {
        Material lowStock = Material.builder()
                .id(2L)
                .currentStock(new BigDecimal("10.00"))
                .reorderLevel(new BigDecimal("20.00"))
                .build();

        when(materialRepository.findLowStockMaterials()).thenReturn(List.of(lowStock));

        List<Material> result = materialService.getLowStockMaterials();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(materialRepository).findLowStockMaterials();
        verify(materialRepository, never()).findLowStockMaterialsByProjectId(any());
    }

    @Test
    @DisplayName("Should get project-filtered low stock materials when projectId is provided")
    void testGetLowStockMaterialsWithProject() {
        Material lowStock = Material.builder()
                .id(3L)
                .currentStock(new BigDecimal("5.00"))
                .reorderLevel(new BigDecimal("15.00"))
                .build();

        when(materialRepository.findLowStockMaterialsByProjectId(100L)).thenReturn(List.of(lowStock));

        List<Material> result = materialService.getLowStockMaterials(100L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(materialRepository).findLowStockMaterialsByProjectId(100L);
    }
}
