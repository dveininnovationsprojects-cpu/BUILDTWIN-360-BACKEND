package com.example.BuildTwin._0.domain.equipment.repository;

import com.example.BuildTwin._0.domain.equipment.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    Optional<Equipment> findByAssetCode(String assetCode);
}
