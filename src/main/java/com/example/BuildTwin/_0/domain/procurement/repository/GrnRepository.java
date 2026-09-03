package com.example.BuildTwin._0.domain.procurement.repository;

import com.example.BuildTwin._0.domain.procurement.model.Grn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrnRepository extends JpaRepository<Grn, Long> {
    List<Grn> findByPoId(Long poId);
}
