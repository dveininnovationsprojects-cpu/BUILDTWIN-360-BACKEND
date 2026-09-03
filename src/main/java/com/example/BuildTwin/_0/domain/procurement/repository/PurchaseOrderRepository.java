package com.example.BuildTwin._0.domain.procurement.repository;

import com.example.BuildTwin._0.domain.procurement.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findByProjectId(Long projectId);
    List<PurchaseOrder> findBySupplierId(Long supplierId);
}
