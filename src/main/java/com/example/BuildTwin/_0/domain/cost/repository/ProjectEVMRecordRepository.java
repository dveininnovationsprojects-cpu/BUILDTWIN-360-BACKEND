package com.example.BuildTwin._0.domain.cost.repository;

import com.example.BuildTwin._0.domain.cost.model.ProjectEVMRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectEVMRecordRepository extends JpaRepository<ProjectEVMRecord, Long> {

    List<ProjectEVMRecord> findByProjectId(Long projectId);

    Page<ProjectEVMRecord> findByProjectId(Long projectId, Pageable pageable);

    List<ProjectEVMRecord> findByProjectIdOrderByEvaluationDateAsc(Long projectId);

    List<ProjectEVMRecord> findByProjectIdOrderByEvaluationDateDesc(Long projectId);

    List<ProjectEVMRecord> findByProjectIdAndEvaluationDateBetweenOrderByEvaluationDateAsc(
            Long projectId, LocalDate startDate, LocalDate endDate);

    Optional<ProjectEVMRecord> findTopByProjectIdOrderByEvaluationDateDesc(Long projectId);

    Optional<ProjectEVMRecord> findByProjectIdAndEvaluationDate(Long projectId, LocalDate evaluationDate);

    boolean existsByProjectIdAndEvaluationDate(Long projectId, LocalDate evaluationDate);

    void deleteByProjectId(Long projectId);
}
