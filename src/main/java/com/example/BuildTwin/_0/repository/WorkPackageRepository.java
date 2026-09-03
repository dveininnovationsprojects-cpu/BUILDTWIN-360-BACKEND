package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.WorkPackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkPackageRepository extends JpaRepository<WorkPackage, Long> {

    List<WorkPackage> findByProjectId(Long projectId);

    Page<WorkPackage> findByProjectId(Long projectId, Pageable pageable);

    Page<WorkPackage> findByProjectIdAndStatus(Long projectId, String status, Pageable pageable);

    Page<WorkPackage> findByProjectIdAndDiscipline(Long projectId, String discipline, Pageable pageable);

    Optional<WorkPackage> findByProjectIdAndCode(Long projectId, String code);

    boolean existsByProjectIdAndCode(Long projectId, String code);

    boolean existsByProjectIdAndCodeAndIdNot(Long projectId, String code, Long id);

    long countByProjectId(Long projectId);

    long countByProjectIdAndStatus(Long projectId, String status);
}
