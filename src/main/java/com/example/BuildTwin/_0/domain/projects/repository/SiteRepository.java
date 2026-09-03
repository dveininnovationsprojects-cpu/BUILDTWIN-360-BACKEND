package com.example.BuildTwin._0.domain.projects.repository;

import com.example.BuildTwin._0.domain.projects.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {
    List<Site> findByProjectId(Long projectId);
}
