package com.example.BuildTwin._0.domain.materials.repository;

import com.example.BuildTwin._0.domain.materials.enums.StockTransactionType;
import com.example.BuildTwin._0.domain.materials.model.StockLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockLedgerRepository extends JpaRepository<StockLedger, Long> {

    List<StockLedger> findByMaterialId(Long materialId);

    List<StockLedger> findByProjectId(Long projectId);

    List<StockLedger> findByActivityId(Long activityId);

    List<StockLedger> findByMaterialIdAndProjectId(Long materialId, Long projectId);

    List<StockLedger> findByProjectIdAndTimestampBetween(Long projectId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(s.quantity) FROM StockLedger s WHERE s.material.id = :materialId AND s.projectId = :projectId AND s.transactionType = :txnType")
    BigDecimal getTotalQuantityByType(@Param("materialId") Long materialId, @Param("projectId") Long projectId, @Param("txnType") StockTransactionType txnType);

    @Query("SELECT COALESCE(SUM(s.quantity * s.unitPrice), 0) FROM StockLedger s WHERE s.projectId = :projectId AND s.transactionType = 'CONSUMPTION'")
    BigDecimal getTotalMaterialConsumptionCostByProjectId(@Param("projectId") Long projectId);
}
