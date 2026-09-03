package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    Page<Project> findByStatus(String status, Pageable pageable);

    Page<Project> findByProjectType(String projectType, Pageable pageable);

    Page<Project> findByStatusAndProjectType(String status, String projectType, Pageable pageable);

    Page<Project> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCaseOrLocationContainingIgnoreCase(
            String name, String code, String location, Pageable pageable);

    Page<Project> findByStatusAndNameContainingIgnoreCaseOrStatusAndCodeContainingIgnoreCaseOrStatusAndLocationContainingIgnoreCase(
            String status1, String name, String status2, String code, String status3, String location, Pageable pageable);

    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(p.estimatedBudget), 0) FROM Project p")
    BigDecimal sumTotalEstimatedBudget();
}
