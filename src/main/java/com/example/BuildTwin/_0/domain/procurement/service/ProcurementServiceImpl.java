package com.example.BuildTwin._0.domain.procurement.service;

import com.example.BuildTwin._0.domain.procurement.model.*;
import com.example.BuildTwin._0.domain.procurement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProcurementServiceImpl implements ProcurementService {

    private final MaterialRequestRepository materialRequestRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final GrnRepository grnRepository;

    @Override
    public MaterialRequest createMaterialRequest(MaterialRequest request) {
        if (request.getStatus() == null) {
            request.setStatus("PENDING");
        }
        return materialRequestRepository.save(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialRequest> getMaterialRequestsByProject(Long projectId) {
        return materialRequestRepository.findByProjectId(projectId);
    }

    @Override
    public PurchaseOrder createPurchaseOrder(PurchaseOrder po) {
        if (po.getStatus() == null) {
            po.setStatus("ISSUED");
        }
        return purchaseOrderRepository.save(po);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrder> getPurchaseOrdersByProject(Long projectId) {
        return purchaseOrderRepository.findByProjectId(projectId);
    }

    @Override
    public Grn createGrn(Grn grn) {
        return grnRepository.save(grn);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Grn> getGrnsByPo(Long poId) {
        return grnRepository.findByPoId(poId);
    }
}
