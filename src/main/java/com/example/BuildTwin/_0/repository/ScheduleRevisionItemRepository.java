package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.ScheduleRevisionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRevisionItemRepository extends JpaRepository<ScheduleRevisionItem, Long> {

    List<ScheduleRevisionItem> findByScheduleRevisionId(Long scheduleRevisionId);

    Optional<ScheduleRevisionItem> findByScheduleRevisionIdAndActivityId(Long scheduleRevisionId, Long activityId);

    boolean existsByScheduleRevisionIdAndActivityId(Long scheduleRevisionId, Long activityId);
}
