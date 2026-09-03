package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.Site;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {

    List<Site> findByProjectId(Long projectId);

    Page<Site> findByProjectId(Long projectId, Pageable pageable);

    Optional<Site> findByProjectIdAndCode(Long projectId, String code);

    boolean existsByProjectIdAndCode(Long projectId, String code);

    boolean existsByProjectIdAndCodeAndIdNot(Long projectId, String code, Long id);

    long countByProjectId(Long projectId);

    long countByStatus(String status);
}
