package com.example.BuildTwin._0.domain.labour.repository;

import com.example.BuildTwin._0.domain.labour.model.LabourDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LabourDailyRepository extends JpaRepository<LabourDaily, Long> {

    List<LabourDaily> findByProjectId(Long projectId);

    List<LabourDaily> findByContractorId(Long contractorId);

    List<LabourDaily> findByProjectIdAndRecordDate(Long projectId, LocalDate recordDate);

    List<LabourDaily> findByContractorIdAndRecordDateBetween(Long contractorId, LocalDate startDate, LocalDate endDate);

    List<LabourDaily> findByProjectIdAndRecordDateBetween(Long projectId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(l.headcount) FROM LabourDaily l WHERE l.projectId = :projectId AND l.recordDate = :recordDate")
    Integer getTotalHeadcountForProjectAndDate(@Param("projectId") Long projectId, @Param("recordDate") LocalDate recordDate);
}
