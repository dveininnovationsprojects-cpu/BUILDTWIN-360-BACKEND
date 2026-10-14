package com.example.BuildTwin._0.domain.cost.repository;

import com.example.BuildTwin._0.domain.cost.model.ProjectBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectBudgetRepository extends JpaRepository<ProjectBudget, Long> {

    List<ProjectBudget> findByProjectId(Long projectId);

    List<ProjectBudget> findByProjectIdAndWbsActivityId(Long projectId, Long wbsActivityId);

    Optional<ProjectBudget> findByProjectIdAndCostCode(Long projectId, String costCode);

    boolean existsByProjectIdAndCostCode(Long projectId, String costCode);

    @Query("SELECT COALESCE(SUM(pb.allocatedAmount), 0) FROM ProjectBudget pb WHERE pb.projectId = :projectId")
    BigDecimal getTotalAllocatedBudgetByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COALESCE(SUM(CASE WHEN pb.revisedAmount IS NOT NULL AND pb.revisedAmount > 0 THEN pb.revisedAmount ELSE pb.allocatedAmount END), 0) FROM ProjectBudget pb WHERE pb.projectId = :projectId")
    BigDecimal getTotalEffectiveBudgetByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COALESCE(SUM(pb.allocatedAmount), 0) FROM ProjectBudget pb WHERE pb.projectId = :projectId AND pb.wbsActivityId = :wbsActivityId")
    BigDecimal getTotalAllocatedBudgetByProjectIdAndWbsActivityId(@Param("projectId") Long projectId, @Param("wbsActivityId") Long wbsActivityId);

    @Query("SELECT COALESCE(SUM(CASE WHEN pb.revisedAmount IS NOT NULL AND pb.revisedAmount > 0 THEN pb.revisedAmount ELSE pb.allocatedAmount END), 0) FROM ProjectBudget pb WHERE pb.projectId = :projectId AND pb.wbsActivityId = :wbsActivityId")
    BigDecimal getTotalEffectiveBudgetByProjectIdAndWbsActivityId(@Param("projectId") Long projectId, @Param("wbsActivityId") Long wbsActivityId);
}

