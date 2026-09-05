package com.example.BuildTwin._0.domain.procurement.repository;

import com.example.BuildTwin._0.domain.procurement.model.MaterialRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRequestRepository extends JpaRepository<MaterialRequest, Long> {
    List<MaterialRequest> findByProjectId(Long projectId);
    List<MaterialRequest> findByProjectIdAndStatus(Long projectId, String status);
    List<MaterialRequest> findByProjectIdAndStatusIn(Long projectId, List<String> statuses);
}
