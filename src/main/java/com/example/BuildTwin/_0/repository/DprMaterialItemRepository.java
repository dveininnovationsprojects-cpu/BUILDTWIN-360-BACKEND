package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.DprMaterialItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DprMaterialItemRepository extends JpaRepository<DprMaterialItem, Long> {

    List<DprMaterialItem> findByDprId(Long dprId);
}
