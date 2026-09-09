package com.example.BuildTwin._0.domain.equipment.service;

import com.example.BuildTwin._0.domain.equipment.model.*;

import java.util.List;

public interface EquipmentService {
    Equipment registerEquipment(Equipment equipment);
    Equipment getEquipmentById(Long id);
    List<Equipment> getAllEquipment();
    Equipment updateEquipment(Long id, Equipment equipment);
    void deleteEquipment(Long id);
    EquipmentUsage recordUsage(EquipmentUsage usage);
    EquipmentUsage getEquipmentUsageById(Long id);
    List<EquipmentUsage> getUsageByProject(Long projectId);
    void deleteEquipmentUsage(Long id);
}
