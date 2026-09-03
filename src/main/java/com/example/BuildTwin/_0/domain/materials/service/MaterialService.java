package com.example.BuildTwin._0.domain.materials.service;

import com.example.BuildTwin._0.domain.materials.model.Material;
import com.example.BuildTwin._0.domain.materials.repository.MaterialRepository;
import com.example.BuildTwin._0.domain.materials.dto.MaterialRequestDto;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;

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
    public List<Material> getLowStockMaterials() {
        return materialRepository.findLowStockMaterials();
    }

    @Transactional(readOnly = true)
    public List<Material> getMaterialsNeedingReorder() {
        return materialRepository.findLowStockMaterials();
    }
}
