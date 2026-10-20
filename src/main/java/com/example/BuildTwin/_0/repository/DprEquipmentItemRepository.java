package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.DprEquipmentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DprEquipmentItemRepository extends JpaRepository<DprEquipmentItem, Long> {

    List<DprEquipmentItem> findByDprId(Long dprId);
}
