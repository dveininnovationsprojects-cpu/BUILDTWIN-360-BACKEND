package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByCode(String code);
    boolean existsByCode(String code);
}
