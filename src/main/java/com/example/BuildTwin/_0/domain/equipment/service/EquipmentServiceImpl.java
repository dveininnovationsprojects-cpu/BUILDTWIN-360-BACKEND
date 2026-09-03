package com.example.BuildTwin._0.domain.equipment.service;

import com.example.BuildTwin._0.domain.equipment.model.*;
import com.example.BuildTwin._0.domain.equipment.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentUsageRepository equipmentUsageRepository;

    @Override
    public Equipment registerEquipment(Equipment equipment) {
        if (equipment.getStatus() == null) {
            equipment.setStatus("AVAILABLE");
        }
        return equipmentRepository.save(equipment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    @Override
    public EquipmentUsage recordUsage(EquipmentUsage usage) {
        return equipmentUsageRepository.save(usage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentUsage> getUsageByProject(Long projectId) {
        return equipmentUsageRepository.findByProjectId(projectId);
    }
}
