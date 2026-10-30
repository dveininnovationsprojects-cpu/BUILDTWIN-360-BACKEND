package com.example.BuildTwin._0.domain.materials.service;

import com.example.BuildTwin._0.domain.materials.dto.MaterialStockBalanceDto;
import com.example.BuildTwin._0.domain.materials.model.Material;
import com.example.BuildTwin._0.domain.materials.repository.MaterialRepository;
import com.example.BuildTwin._0.domain.materials.repository.StockLedgerRepository;
import com.example.BuildTwin._0.domain.materials.dto.MaterialRequestDto;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final StockLedgerRepository stockLedgerRepository;

    @Transactional
    public Material createMaterial(MaterialRequestDto dto) {
        if (materialRepository.existsByMaterialCode(dto.getMaterialCode())) {
            throw new DuplicateResourceException("Material already exists with code: " + dto.getMaterialCode());
        }

        Material material = Material.builder()
                .materialCode(dto.getMaterialCode())
                .name(dto.getName())
                .category(dto.getCategory())
                .unit(dto.getUnit())
                .standardRate(dto.getStandardRate())
                .reorderLevel(dto.getReorderLevel() != null ? dto.getReorderLevel() : BigDecimal.ZERO)
                .currentStock(BigDecimal.ZERO)
                .description(dto.getDescription())
                .build();

        return materialRepository.save(material);
    }

    @Transactional
    public Material updateMaterial(Long id, com.example.BuildTwin._0.domain.materials.dto.MaterialUpdateDto dto) {
        Material material = getMaterialById(id);

        if (dto.getName() != null && !dto.getName().isBlank()) {
            material.setName(dto.getName());
        }
        if (dto.getCategory() != null && !dto.getCategory().isBlank()) {
            material.setCategory(dto.getCategory());
        }
        if (dto.getUnit() != null) {
            material.setUnit(dto.getUnit());
        }
        if (dto.getStandardRate() != null) {
            material.setStandardRate(dto.getStandardRate());
        }
        if (dto.getReorderLevel() != null) {
            material.setReorderLevel(dto.getReorderLevel());
        }
        if (dto.getDescription() != null) {
            material.setDescription(dto.getDescription());
        }

        return materialRepository.save(material);
    }

    @Transactional(readOnly = true)
    public Material getMaterialById(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material", "id", id));
    }

    @Transactional(readOnly = true)
    public Material getMaterialByCode(String code) {
        return materialRepository.findByMaterialCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Material", "materialCode", code));
    }

    @Transactional(readOnly = true)
    public List<Material> getAllMaterials() {
        return materialRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Material> getMaterialsByCategory(String category) {
        return materialRepository.findByCategory(category);
    }

    @Transactional(readOnly = true)
    public MaterialStockBalanceDto getStockBalance(Long id) {
        Material material = getMaterialById(id);
        BigDecimal currentStock = material.getCurrentStock() != null ? material.getCurrentStock() : BigDecimal.ZERO;
        BigDecimal reorderLevel = material.getReorderLevel() != null ? material.getReorderLevel() : BigDecimal.ZERO;
        BigDecimal standardRate = material.getStandardRate() != null ? material.getStandardRate() : BigDecimal.ZERO;
        BigDecimal totalValue = currentStock.multiply(standardRate).setScale(2, java.math.RoundingMode.HALF_UP);
        boolean lowStock = material.getReorderLevel() != null && currentStock.compareTo(reorderLevel) <= 0;

        LocalDateTime lastTxnTime = stockLedgerRepository.findLatestTimestampByMaterialId(id).orElse(null);

        return MaterialStockBalanceDto.builder()
                .materialId(material.getId())
                .materialCode(material.getMaterialCode())
                .name(material.getName())
                .category(material.getCategory())
                .unit(material.getUnit())
                .currentStock(currentStock)
                .reorderLevel(reorderLevel)
                .standardRate(standardRate)
                .totalStockValue(totalValue)
                .lowStock(lowStock)
                .lastTransactionTimestamp(lastTxnTime)
                .build();
    }

    @Transactional(readOnly = true)
    public List<Material> getLowStockMaterials() {
        return getLowStockMaterials(null);
    }

    @Transactional(readOnly = true)
    public List<Material> getLowStockMaterials(Long projectId) {
        if (projectId != null) {
            return materialRepository.findLowStockMaterialsByProjectId(projectId);
        }
        return materialRepository.findLowStockMaterials();
    }

    @Transactional(readOnly = true)
    public List<Material> getMaterialsNeedingReorder() {
        return materialRepository.findLowStockMaterials();
    }

    @Transactional
    public void deleteMaterial(Long id) {
        Material material = getMaterialById(id);
        materialRepository.delete(material);
    }
}
