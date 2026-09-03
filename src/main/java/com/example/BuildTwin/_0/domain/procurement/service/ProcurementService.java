package com.example.BuildTwin._0.domain.procurement.service;

import com.example.BuildTwin._0.domain.procurement.model.*;

import java.util.List;

public interface ProcurementService {
    MaterialRequest createMaterialRequest(MaterialRequest request);
    List<MaterialRequest> getMaterialRequestsByProject(Long projectId);
    PurchaseOrder createPurchaseOrder(PurchaseOrder po);
    List<PurchaseOrder> getPurchaseOrdersByProject(Long projectId);
    Grn createGrn(Grn grn);
    List<Grn> getGrnsByPo(Long poId);
}
