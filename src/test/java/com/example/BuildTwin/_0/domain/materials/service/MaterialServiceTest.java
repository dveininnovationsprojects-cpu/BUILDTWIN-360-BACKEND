package com.example.BuildTwin._0.domain.materials.service;

import com.example.BuildTwin._0.domain.materials.dto.MaterialRequestDto;
import com.example.BuildTwin._0.domain.materials.enums.MaterialUnit;
import com.example.BuildTwin._0.domain.materials.model.Material;
import com.example.BuildTwin._0.domain.materials.repository.MaterialRepository;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {

    @Mock
    private MaterialRepository materialRepository;

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
}
