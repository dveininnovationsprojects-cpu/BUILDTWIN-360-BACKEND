package com.example.BuildTwin._0.domain.materials.repository;

import com.example.BuildTwin._0.domain.materials.model.Material;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    Optional<Material> findByMaterialCode(String materialCode);

    boolean existsByMaterialCode(String materialCode);

    List<Material> findByCategory(String category);

    @Query("SELECT m FROM Material m WHERE m.reorderLevel IS NOT NULL AND m.currentStock <= m.reorderLevel")
    List<Material> findLowStockMaterials();

    @Query("SELECT DISTINCT s.material FROM StockLedger s WHERE s.projectId = :projectId AND s.material.reorderLevel IS NOT NULL AND s.material.currentStock <= s.material.reorderLevel")
    List<Material> findLowStockMaterialsByProjectId(@Param("projectId") Long projectId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Material m WHERE m.id = :id")
    Optional<Material> findByIdForUpdate(@Param("id") Long id);
}
