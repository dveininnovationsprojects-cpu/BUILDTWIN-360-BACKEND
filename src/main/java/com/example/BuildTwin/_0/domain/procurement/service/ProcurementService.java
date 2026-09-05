package com.example.BuildTwin._0.domain.procurement.service;

import com.example.BuildTwin._0.domain.procurement.model.*;

import java.util.List;

public interface ProcurementService {
    MaterialRequest createMaterialRequest(MaterialRequest request);
    MaterialRequest updateMaterialRequestStatus(Long requestId, com.example.BuildTwin._0.domain.procurement.dto.MaterialRequestApprovalDto approvalDto);
    List<MaterialRequest> getMaterialRequestsByProject(Long projectId);
    List<MaterialRequest> getMaterialRequestsByStatus(Long projectId, String status);
    List<com.example.BuildTwin._0.domain.materials.dto.ProjectedShortageDto> detectProjectedShortage(Long projectId);
    PurchaseOrder createPurchaseOrder(PurchaseOrder po);
    List<PurchaseOrder> getPurchaseOrdersByProject(Long projectId);
    Grn createGrn(Grn grn);
    List<Grn> getGrnsByPo(Long poId);
}
