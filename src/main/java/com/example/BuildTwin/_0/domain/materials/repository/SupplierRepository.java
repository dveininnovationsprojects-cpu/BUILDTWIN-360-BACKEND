package com.example.BuildTwin._0.domain.materials.repository;

import com.example.BuildTwin._0.domain.materials.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findBySupplierCode(String supplierCode);

    boolean existsBySupplierCode(String supplierCode);

    List<Supplier> findByStatus(String status);
}
