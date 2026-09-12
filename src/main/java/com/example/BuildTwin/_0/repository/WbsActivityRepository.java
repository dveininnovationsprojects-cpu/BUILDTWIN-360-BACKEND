package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.WbsActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WbsActivityRepository extends JpaRepository<WbsActivity, Long> {

    List<WbsActivity> findByWorkPackageIdOrderBySequenceOrderAsc(Long workPackageId);

    List<WbsActivity> findByProjectIdOrderBySequenceOrderAsc(Long projectId);

    Page<WbsActivity> findByWorkPackageId(Long workPackageId, Pageable pageable);

    Page<WbsActivity> findByWorkPackageIdAndStatus(Long workPackageId, String status, Pageable pageable);

    Page<WbsActivity> findByProjectId(Long projectId, Pageable pageable);

    Page<WbsActivity> findByProjectIdAndStatus(Long projectId, String status, Pageable pageable);

    Page<WbsActivity> findByProjectIdAndDiscipline(Long projectId, String discipline, Pageable pageable);

    Optional<WbsActivity> findByWorkPackageIdAndCode(Long workPackageId, String code);

    boolean existsByWorkPackageIdAndCode(Long workPackageId, String code);

    boolean existsByWorkPackageIdAndCodeAndIdNot(Long workPackageId, String code, Long id);

    long countByWorkPackageId(Long workPackageId);

    long countByWorkPackageIdAndStatus(Long workPackageId, String status);

    long countByProjectId(Long projectId);

    long countByProjectIdAndStatus(Long projectId, String status);

    List<WbsActivity> findBySiteId(Long siteId);

    List<WbsActivity> findByZoneId(Long zoneId);
}
