package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.Floor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FloorRepository extends JpaRepository<Floor, Long> {

    List<Floor> findByBuildingIdOrderByFloorNumberAsc(Long buildingId);

    Page<Floor> findByBuildingId(Long buildingId, Pageable pageable);

    Optional<Floor> findByBuildingIdAndFloorNumber(Long buildingId, Integer floorNumber);

    boolean existsByBuildingIdAndFloorNumber(Long buildingId, Integer floorNumber);

    boolean existsByBuildingIdAndFloorNumberAndIdNot(Long buildingId, Integer floorNumber, Long id);

    long countByBuildingId(Long buildingId);
}
