package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.ActivityDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityDependencyRepository extends JpaRepository<ActivityDependency, Long> {

    List<ActivityDependency> findByProjectId(Long projectId);

    List<ActivityDependency> findByPredecessorId(Long predecessorId);

    List<ActivityDependency> findBySuccessorId(Long successorId);

    List<ActivityDependency> findByPredecessorIdOrSuccessorId(Long predecessorId, Long successorId);

    Optional<ActivityDependency> findByPredecessorIdAndSuccessorId(Long predecessorId, Long successorId);

    boolean existsByPredecessorIdAndSuccessorId(Long predecessorId, Long successorId);

    boolean existsByPredecessorIdAndSuccessorIdAndIdNot(Long predecessorId, Long successorId, Long id);

    @Query("SELECT d FROM ActivityDependency d WHERE d.predecessor.workPackage.id = :workPackageId OR d.successor.workPackage.id = :workPackageId")
    List<ActivityDependency> findByWorkPackageId(@Param("workPackageId") Long workPackageId);

    @Modifying
    @Query("DELETE FROM ActivityDependency d WHERE d.predecessor.id = :activityId OR d.successor.id = :activityId")
    void deleteByActivityId(@Param("activityId") Long activityId);

    @Modifying
    @Query("DELETE FROM ActivityDependency d WHERE d.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);
}
