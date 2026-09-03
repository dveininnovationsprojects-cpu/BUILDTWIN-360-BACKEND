package com.example.BuildTwin._0.domain.labour.repository;

import com.example.BuildTwin._0.domain.labour.model.LabourAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LabourAllocationRepository extends JpaRepository<LabourAllocation, Long> {

    List<LabourAllocation> findByWbsActivityId(Long wbsActivityId);

    @Query("SELECT COALESCE(SUM(la.hoursAllocated), 0) FROM LabourAllocation la WHERE la.wbsActivityId = :wbsActivityId")
    BigDecimal getTotalHoursAllocatedByActivity(@Param("wbsActivityId") Long wbsActivityId);
}
