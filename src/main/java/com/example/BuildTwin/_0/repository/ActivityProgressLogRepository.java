package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.ActivityProgressLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityProgressLogRepository extends JpaRepository<ActivityProgressLog, Long> {

    List<ActivityProgressLog> findByActivityIdOrderByLogDateDesc(Long activityId);

    List<ActivityProgressLog> findByActivityIdAndLogDateBetweenOrderByLogDateAsc(Long activityId, LocalDate startDate, LocalDate endDate);

    Optional<ActivityProgressLog> findTopByActivityIdOrderByLogDateDesc(Long activityId);

    void deleteByActivityId(Long activityId);
}
