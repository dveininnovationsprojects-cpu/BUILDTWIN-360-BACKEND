package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.DprLaborItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DprLaborItemRepository extends JpaRepository<DprLaborItem, Long> {

    List<DprLaborItem> findByDprId(Long dprId);
}
