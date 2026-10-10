package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.DprActivityItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DprActivityItemRepository extends JpaRepository<DprActivityItem, Long> {

    List<DprActivityItem> findByDprId(Long dprId);

    List<DprActivityItem> findByActivityId(Long activityId);
}
