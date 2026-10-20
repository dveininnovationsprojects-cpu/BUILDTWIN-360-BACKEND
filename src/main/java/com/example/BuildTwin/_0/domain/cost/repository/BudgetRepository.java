package com.example.BuildTwin._0.domain.cost.repository;

import com.example.BuildTwin._0.domain.cost.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByProjectId(Long projectId);

    List<Budget> findByProjectIdAndActivityId(Long projectId, Long activityId);

    Optional<Budget> findByProjectIdAndCostCode(Long projectId, String costCode);

    List<Budget> findByProjectIdAndCostHead(Long projectId, com.example.BuildTwin._0.domain.cost.enums.CostHead costHead);

    @Query("SELECT COALESCE(SUM(b.baselineAmount), 0) FROM Budget b WHERE b.projectId = :projectId")
    BigDecimal getTotalBaselineBudgetByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COALESCE(SUM(CASE WHEN b.revisedAmount > 0 THEN b.revisedAmount ELSE b.baselineAmount END), 0) FROM Budget b WHERE b.projectId = :projectId")
    BigDecimal getTotalApprovedBudgetByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COALESCE(SUM(b.baselineAmount), 0) FROM Budget b WHERE b.projectId = :projectId AND b.costHead = :costHead")
    BigDecimal getTotalBaselineByProjectIdAndCostHead(@Param("projectId") Long projectId, @Param("costHead") com.example.BuildTwin._0.domain.cost.enums.CostHead costHead);

    @Query("SELECT COALESCE(SUM(CASE WHEN b.revisedAmount > 0 THEN b.revisedAmount ELSE b.baselineAmount END), 0) FROM Budget b WHERE b.projectId = :projectId AND b.costHead = :costHead")
    BigDecimal getTotalApprovedByProjectIdAndCostHead(@Param("projectId") Long projectId, @Param("costHead") com.example.BuildTwin._0.domain.cost.enums.CostHead costHead);
}
