package com.example.BuildTwin._0.domain.wbs.repository;

import com.example.BuildTwin._0.domain.wbs.model.ActivityDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityDependencyRepository extends JpaRepository<ActivityDependency, Long> {
    List<ActivityDependency> findByPredecessorId(Long predecessorId);
    List<ActivityDependency> findBySuccessorId(Long successorId);
}
