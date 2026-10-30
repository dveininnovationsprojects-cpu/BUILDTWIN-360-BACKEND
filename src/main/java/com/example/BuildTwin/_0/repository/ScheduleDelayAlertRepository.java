package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.DelayAlertStatus;
import com.example.BuildTwin._0.model.DelayAlertType;
import com.example.BuildTwin._0.model.OverdueSeverity;
import com.example.BuildTwin._0.model.ScheduleDelayAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleDelayAlertRepository extends JpaRepository<ScheduleDelayAlert, Long>, JpaSpecificationExecutor<ScheduleDelayAlert> {

    List<ScheduleDelayAlert> findByProjectIdOrderByTriggeredAtDesc(Long projectId);

    List<ScheduleDelayAlert> findByProjectIdAndStatusOrderByTriggeredAtDesc(Long projectId, DelayAlertStatus status);

    List<ScheduleDelayAlert> findByProjectIdAndStatusInOrderBySeverityDescTriggeredAtDesc(Long projectId, Collection<DelayAlertStatus> statuses);

    List<ScheduleDelayAlert> findByProjectIdAndActivityIdOrderByTriggeredAtDesc(Long projectId, Long activityId);

    List<ScheduleDelayAlert> findByProjectIdAndActivityIdAndStatusIn(Long projectId, Long activityId, Collection<DelayAlertStatus> statuses);

    Optional<ScheduleDelayAlert> findByProjectIdAndActivityIdAndAlertTypeAndStatusNotIn(
            Long projectId, Long activityId, DelayAlertType alertType, Collection<DelayAlertStatus> closedStatuses);

    Optional<ScheduleDelayAlert> findByAlertCode(String alertCode);

    long countByProjectId(Long projectId);

    long countByProjectIdAndStatus(Long projectId, DelayAlertStatus status);

    long countByProjectIdAndSeverityAndStatus(Long projectId, OverdueSeverity severity, DelayAlertStatus status);

    @Query("SELECT COUNT(a) FROM ScheduleDelayAlert a WHERE a.project.id = :projectId AND a.status IN :statuses")
    long countByProjectIdAndStatusIn(@Param("projectId") Long projectId, @Param("statuses") Collection<DelayAlertStatus> statuses);
}
