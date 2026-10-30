package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.DailyProgressReport;
import com.example.BuildTwin._0.model.DprStatus;
import com.example.BuildTwin._0.model.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyProgressReportRepository extends JpaRepository<DailyProgressReport, Long> {

    List<DailyProgressReport> findByProjectIdOrderByReportDateDesc(Long projectId);

    List<DailyProgressReport> findByProjectIdAndReportDateBetweenOrderByReportDateAsc(
            Long projectId, LocalDate startDate, LocalDate endDate);

    List<DailyProgressReport> findByProjectIdAndStatusOrderByReportDateDesc(
            Long projectId, DprStatus status);

    Optional<DailyProgressReport> findByProjectIdAndReportDateAndShift(
            Long projectId, LocalDate reportDate, WorkShift shift);

    Optional<DailyProgressReport> findByReportCode(String reportCode);

    boolean existsByProjectIdAndReportDateAndShift(
            Long projectId, LocalDate reportDate, WorkShift shift);

    Optional<DailyProgressReport> findTopByProjectIdOrderByReportDateDesc(Long projectId);

    Long countByProjectId(Long projectId);

    Long countByProjectIdAndStatus(Long projectId, DprStatus status);

    @Query("SELECT COALESCE(SUM(d.totalManpowerCount), 0) FROM DailyProgressReport d WHERE d.project.id = :projectId")
    Long sumManpowerByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COALESCE(SUM(d.safetyIncidentsCount), 0) FROM DailyProgressReport d WHERE d.project.id = :projectId")
    Long sumSafetyIncidentsByProjectId(@Param("projectId") Long projectId);
}
