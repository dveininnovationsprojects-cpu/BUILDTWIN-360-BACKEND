package com.example.BuildTwin._0.domain.procurement.service;

import com.example.BuildTwin._0.domain.procurement.model.*;
import com.example.BuildTwin._0.domain.procurement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.example.BuildTwin._0.domain.materials.dto.ProjectedShortageDto;
import com.example.BuildTwin._0.domain.materials.dto.StockTransactionDto;
import com.example.BuildTwin._0.domain.materials.enums.StockTransactionType;
import com.example.BuildTwin._0.domain.materials.model.Material;
import com.example.BuildTwin._0.domain.materials.repository.MaterialRepository;
import com.example.BuildTwin._0.domain.materials.service.StockLedgerService;
import com.example.BuildTwin._0.domain.procurement.dto.MaterialRequestApprovalDto;
import com.example.BuildTwin._0.exception.BadRequestException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ProcurementServiceImpl implements ProcurementService {

    private final MaterialRequestRepository materialRequestRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final GrnRepository grnRepository;
    private final MaterialRepository materialRepository;
    private final StockLedgerService stockLedgerService;

    @Override
    public MaterialRequest createMaterialRequest(MaterialRequest request) {
        if (request.getStatus() == null || request.getStatus().isBlank()) {
            request.setStatus("PENDING");
        } else {
            request.setStatus(request.getStatus().toUpperCase());
        }
        return materialRequestRepository.save(request);
    }

    @Override
    public MaterialRequest updateMaterialRequestStatus(Long requestId, MaterialRequestApprovalDto approvalDto) {
        MaterialRequest request = materialRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("MaterialRequest", "id", requestId));

        String newStatus = approvalDto.getStatus().toUpperCase();
        if (!List.of("APPROVED", "REJECTED", "PENDING", "ORDERED").contains(newStatus)) {
            throw new BadRequestException("Invalid material request status: " + approvalDto.getStatus() + ". Must be APPROVED, REJECTED, or PENDING.");
        }

        request.setStatus(newStatus);
        if ("REJECTED".equals(newStatus)) {
            request.setRejectionReason(approvalDto.getRejectionReason());
        }
        if (approvalDto.getApprovedBy() != null) {
            request.setApprovedBy(approvalDto.getApprovedBy());
        }
        if (approvalDto.getRemarks() != null) {
            request.setRemarks(approvalDto.getRemarks());
        }

        return materialRequestRepository.save(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialRequest> getMaterialRequestsByProject(Long projectId) {
        return materialRequestRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialRequest> getMaterialRequestsByStatus(Long projectId, String status) {
        return materialRequestRepository.findByProjectIdAndStatus(projectId, status.toUpperCase());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectedShortageDto> detectProjectedShortage(Long projectId) {
        List<MaterialRequest> activeRequests = materialRequestRepository.findByProjectIdAndStatusIn(
                projectId, List.of("PENDING", "APPROVED")
        );

        Map<Long, BigDecimal> requestedQtyMap = new HashMap<>();
        for (MaterialRequest req : activeRequests) {
            requestedQtyMap.merge(req.getMaterialId(), req.getRequiredQty(), BigDecimal::add);
        }

        List<ProjectedShortageDto> result = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : requestedQtyMap.entrySet()) {
            Long materialId = entry.getKey();
            BigDecimal totalRequested = entry.getValue();

            Material material = materialRepository.findById(materialId).orElse(null);
            if (material == null) continue;

            BigDecimal currentStock = material.getCurrentStock() != null ? material.getCurrentStock() : BigDecimal.ZERO;
            BigDecimal reorderLevel = material.getReorderLevel() != null ? material.getReorderLevel() : BigDecimal.ZERO;
            BigDecimal shortage = totalRequested.compareTo(currentStock) > 0
                    ? totalRequested.subtract(currentStock)
                    : BigDecimal.ZERO;

            String status = "OPTIMAL";
            if (shortage.compareTo(BigDecimal.ZERO) > 0) {
                status = "SHORTAGE";
            } else if (currentStock.compareTo(reorderLevel) <= 0) {
                status = "LOW_STOCK";
            }

            result.add(ProjectedShortageDto.builder()
                    .materialId(material.getId())
                    .materialCode(material.getMaterialCode())
                    .materialName(material.getName())
                    .category(material.getCategory())
                    .unit(material.getUnit())
                    .currentStock(currentStock)
                    .reorderLevel(reorderLevel)
                    .totalRequestedQty(totalRequested)
                    .projectedShortage(shortage)
                    .status(status)
                    .build());
        }

        return result;
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
        BigDecimal received = grn.getReceivedQty() != null ? grn.getReceivedQty() : BigDecimal.ZERO;
        BigDecimal accepted = grn.getAcceptedQty() != null ? grn.getAcceptedQty() : received;
        BigDecimal rejected = grn.getRejectedQty() != null ? grn.getRejectedQty() : BigDecimal.ZERO;

        if (accepted.compareTo(BigDecimal.ZERO) < 0 || rejected.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Accepted and Rejected quantities cannot be negative.");
        }

        if (received.compareTo(BigDecimal.ZERO) > 0 && accepted.add(rejected).compareTo(received) > 0) {
            throw new BadRequestException("Sum of accepted and rejected quantities (" + accepted.add(rejected) + ") cannot exceed received quantity (" + received + ").");
        }

        grn.setAcceptedQty(accepted);
        grn.setRejectedQty(rejected);
        if (grn.getReceivedQty() == null) {
            grn.setReceivedQty(accepted.add(rejected));
        }

        Grn savedGrn = grnRepository.save(grn);

        // Transactionally update inventory stock & record immutable ledger receipt for accepted quantity
        if (accepted.compareTo(BigDecimal.ZERO) > 0) {
            StockTransactionDto stockTxn = StockTransactionDto.builder()
                    .projectId(grn.getProjectId() != null ? grn.getProjectId() : 1L)
                    .siteId(grn.getSiteId())
                    .materialId(grn.getMaterialId())
                    .transactionType(StockTransactionType.RECEIPT)
                    .quantity(accepted)
                    .referenceId("GRN-" + savedGrn.getId())
                    .remarks("GRN Entry: Received " + grn.getReceivedQty() + ", Accepted " + accepted + ", Rejected " + rejected)
                    .build();

            stockLedgerService.recordTransaction(stockTxn);
        }

        return savedGrn;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Grn> getGrnsByPo(Long poId) {
        return grnRepository.findByPoId(poId);
    }
}
