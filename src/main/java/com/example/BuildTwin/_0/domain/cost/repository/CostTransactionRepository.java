package com.example.BuildTwin._0.domain.cost.repository;

import com.example.BuildTwin._0.domain.cost.enums.CostSourceType;
import com.example.BuildTwin._0.domain.cost.model.CostTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CostTransactionRepository extends JpaRepository<CostTransaction, Long> {

    List<CostTransaction> findByProjectId(Long projectId);

    List<CostTransaction> findByProjectIdAndCostCode(Long projectId, String costCode);

    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CostTransaction ct WHERE ct.projectId = :projectId AND ct.sourceType = :sourceType")
    BigDecimal getTotalCostByProjectIdAndSourceType(@Param("projectId") Long projectId, @Param("sourceType") CostSourceType sourceType);

    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CostTransaction ct WHERE ct.projectId = :projectId")
    BigDecimal getTotalActualCostByProjectId(@Param("projectId") Long projectId);
}
