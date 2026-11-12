package com.example.BuildTwin._0.domain.wbs.repository;

import com.example.BuildTwin._0.domain.wbs.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByProjectId(Long projectId);

    @Query("SELECT a FROM Activity a WHERE a.projectId = :projectId " +
           "AND a.status != 'COMPLETED' " +
           "AND a.plannedStartDate <= :endDate " +
           "AND (a.plannedEndDate >= :startDate OR a.plannedEndDate IS NULL)")
    List<Activity> findLookaheadActivities(@Param("projectId") Long projectId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
}
