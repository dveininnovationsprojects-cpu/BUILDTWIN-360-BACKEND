package com.example.BuildTwin._0.domain.equipment.service;

import com.example.BuildTwin._0.domain.equipment.model.*;

import java.util.List;

public interface EquipmentService {
    Equipment registerEquipment(Equipment equipment);
    List<Equipment> getAllEquipment();
    EquipmentUsage recordUsage(EquipmentUsage usage);
    List<EquipmentUsage> getUsageByProject(Long projectId);
}
