package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.BaselineActivitySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BaselineActivitySnapshotRepository extends JpaRepository<BaselineActivitySnapshot, Long> {

    List<BaselineActivitySnapshot> findByBaselineIdOrderByActivityCodeAsc(Long baselineId);

    Optional<BaselineActivitySnapshot> findByBaselineIdAndActivityId(Long baselineId, Long activityId);

    void deleteByBaselineId(Long baselineId);
}
