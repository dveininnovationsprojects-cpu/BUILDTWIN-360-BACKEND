package com.example.BuildTwin._0.domain.dpr.repository;

import com.example.BuildTwin._0.domain.dpr.model.DprActivityProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DprActivityProgressRepository extends JpaRepository<DprActivityProgress, Long> {
    List<DprActivityProgress> findByDprHeaderId(Long dprHeaderId);
    List<DprActivityProgress> findByActivityId(Long activityId);
}
