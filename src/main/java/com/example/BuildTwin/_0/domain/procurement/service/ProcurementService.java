package com.example.BuildTwin._0.domain.procurement.service;

import com.example.BuildTwin._0.domain.procurement.model.*;

import java.util.List;

public interface ProcurementService {
    MaterialRequest createMaterialRequest(MaterialRequest request);
    MaterialRequest updateMaterialRequestStatus(Long requestId, com.example.BuildTwin._0.domain.procurement.dto.MaterialRequestApprovalDto approvalDto);
    MaterialRequest getMaterialRequestById(Long id);
    List<MaterialRequest> getMaterialRequestsByProject(Long projectId);
    List<MaterialRequest> getMaterialRequestsByStatus(Long projectId, String status);
    void deleteMaterialRequest(Long id);
    MaterialRequest updateMaterialRequest(Long id, MaterialRequest request);
    List<com.example.BuildTwin._0.domain.materials.dto.ProjectedShortageDto> detectProjectedShortage(Long projectId);
    PurchaseOrder createPurchaseOrder(PurchaseOrder po);
    PurchaseOrder updatePurchaseOrder(Long id, PurchaseOrder po);
    PurchaseOrder updatePurchaseOrderApproval(Long id, com.example.BuildTwin._0.domain.procurement.dto.PurchaseOrderApprovalDto approvalDto);
    PurchaseOrder getPurchaseOrderById(Long id);
    List<PurchaseOrder> getPurchaseOrdersByProject(Long projectId);
    com.example.BuildTwin._0.domain.procurement.dto.PoFulfillmentDto getPoFulfillmentStatus(Long id);
    void deletePurchaseOrder(Long id);
    Grn createGrn(Grn grn);
    Grn getGrnById(Long id);
    List<Grn> getGrnsByPo(Long poId);
    void deleteGrn(Long id);
}
