package com.example.BuildTwin._0.domain.projects.repository;

import com.example.BuildTwin._0.domain.projects.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    List<Zone> findByLevelId(Long levelId);
}
