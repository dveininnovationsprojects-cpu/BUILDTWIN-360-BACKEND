package com.example.BuildTwin._0.domain.cost.repository;

import com.example.BuildTwin._0.domain.cost.model.CostActualLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CostActualLedgerRepository extends JpaRepository<CostActualLedger, Long> {

    List<CostActualLedger> findByProjectId(Long projectId);

    List<CostActualLedger> findByProjectIdAndCostCode(Long projectId, String costCode);

    List<CostActualLedger> findByProjectIdAndSourceType(Long projectId, String sourceType);

    boolean existsBySourceTypeAndSourceReferenceId(String sourceType, Long sourceReferenceId);

    List<CostActualLedger> findByProjectIdAndWbsActivityId(Long projectId, Long wbsActivityId);

    @Query("SELECT COALESCE(SUM(c.totalCost), 0) FROM CostActualLedger c WHERE c.projectId = :projectId")
    BigDecimal getTotalActualCostByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COALESCE(SUM(c.totalCost), 0) FROM CostActualLedger c WHERE c.projectId = :projectId AND c.costCode = :costCode")
    BigDecimal getTotalActualCostByProjectIdAndCostCode(@Param("projectId") Long projectId, @Param("costCode") String costCode);

    @Query("SELECT COALESCE(SUM(c.totalCost), 0) FROM CostActualLedger c WHERE c.projectId = :projectId AND c.wbsActivityId = :wbsActivityId")
    BigDecimal getTotalActualCostByProjectIdAndWbsActivityId(@Param("projectId") Long projectId, @Param("wbsActivityId") Long wbsActivityId);
}

