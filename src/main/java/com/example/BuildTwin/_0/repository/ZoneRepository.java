package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {

    List<Zone> findByFloorId(Long floorId);

    Page<Zone> findByFloorId(Long floorId, Pageable pageable);

    Optional<Zone> findByFloorIdAndCode(Long floorId, String code);

    boolean existsByFloorIdAndCode(Long floorId, String code);

    boolean existsByFloorIdAndCodeAndIdNot(Long floorId, String code, Long id);

    long countByFloorId(Long floorId);
}
