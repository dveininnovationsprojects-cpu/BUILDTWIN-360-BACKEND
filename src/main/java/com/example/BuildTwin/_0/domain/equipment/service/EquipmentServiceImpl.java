package com.example.BuildTwin._0.domain.equipment.service;

import com.example.BuildTwin._0.domain.equipment.model.*;
import com.example.BuildTwin._0.domain.equipment.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BuildTwin._0.exception.ResourceNotFoundException;

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
    public Equipment getEquipmentById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    @Override
    public Equipment updateEquipment(Long id, Equipment request) {
        Equipment existing = getEquipmentById(id);
        if (request.getAssetCode() != null) existing.setAssetCode(request.getAssetCode());
        if (request.getName() != null) existing.setName(request.getName());
        if (request.getType() != null) existing.setType(request.getType());
        if (request.getStatus() != null) existing.setStatus(request.getStatus());
        return equipmentRepository.save(existing);
    }

    @Override
    public void deleteEquipment(Long id) {
        Equipment equipment = getEquipmentById(id);
        equipmentRepository.delete(equipment);
    }

    @Override
    public EquipmentUsage recordUsage(EquipmentUsage usage) {
        return equipmentUsageRepository.save(usage);
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentUsage getEquipmentUsageById(Long id) {
        return equipmentUsageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EquipmentUsage not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentUsage> getUsageByProject(Long projectId) {
        return equipmentUsageRepository.findByProjectId(projectId);
    }

    @Override
    public void deleteEquipmentUsage(Long id) {
        EquipmentUsage usage = getEquipmentUsageById(id);
        equipmentUsageRepository.delete(usage);
    }
}
