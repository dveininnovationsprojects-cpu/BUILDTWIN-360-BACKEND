package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.ScheduleRevision;
import com.example.BuildTwin._0.model.ScheduleRevisionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRevisionRepository extends JpaRepository<ScheduleRevision, Long> {

    List<ScheduleRevision> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    List<ScheduleRevision> findByProjectIdAndStatusOrderByCreatedAtDesc(Long projectId, ScheduleRevisionStatus status);

    Optional<ScheduleRevision> findTopByProjectIdOrderByCreatedAtDesc(Long projectId);

    boolean existsByProjectIdAndRevisionCodeIgnoreCase(Long projectId, String revisionCode);

    long countByProjectId(Long projectId);
}
