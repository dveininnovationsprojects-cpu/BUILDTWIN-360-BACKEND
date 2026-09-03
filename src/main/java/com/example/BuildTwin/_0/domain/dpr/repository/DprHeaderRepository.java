package com.example.BuildTwin._0.domain.dpr.repository;

import com.example.BuildTwin._0.domain.dpr.model.DprHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DprHeaderRepository extends JpaRepository<DprHeader, Long> {
    List<DprHeader> findByProjectId(Long projectId);
    Optional<DprHeader> findByProjectIdAndReportDate(Long projectId, LocalDate reportDate);
}
