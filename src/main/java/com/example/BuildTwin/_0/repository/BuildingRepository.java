package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.Building;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {

    List<Building> findBySiteId(Long siteId);

    Page<Building> findBySiteId(Long siteId, Pageable pageable);

    Optional<Building> findBySiteIdAndCode(Long siteId, String code);

    boolean existsBySiteIdAndCode(Long siteId, String code);

    boolean existsBySiteIdAndCodeAndIdNot(Long siteId, String code, Long id);

    long countBySiteId(Long siteId);
}
