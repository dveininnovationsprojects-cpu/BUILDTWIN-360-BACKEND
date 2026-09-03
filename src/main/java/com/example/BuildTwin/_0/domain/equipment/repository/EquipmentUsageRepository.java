package com.example.BuildTwin._0.domain.equipment.repository;

import com.example.BuildTwin._0.domain.equipment.model.EquipmentUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentUsageRepository extends JpaRepository<EquipmentUsage, Long> {
    List<EquipmentUsage> findByProjectId(Long projectId);
    List<EquipmentUsage> findByEquipmentId(Long equipmentId);
}
