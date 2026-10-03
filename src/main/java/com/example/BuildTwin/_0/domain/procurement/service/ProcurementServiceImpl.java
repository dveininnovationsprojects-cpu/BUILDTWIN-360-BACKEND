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
    public MaterialRequest getMaterialRequestById(Long id) {
        return materialRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaterialRequest", "id", id));
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
    public void deleteMaterialRequest(Long id) {
        MaterialRequest request = getMaterialRequestById(id);
        materialRequestRepository.delete(request);
    }

    @Override
    public MaterialRequest updateMaterialRequest(Long id, MaterialRequest request) {
        MaterialRequest existing = getMaterialRequestById(id);
        if (!"PENDING".equalsIgnoreCase(existing.getStatus())) {
            throw new BadRequestException("Cannot update material request in '" + existing.getStatus() + "' status. Only PENDING requests can be edited.");
        }
        if (request.getRequiredQty() != null) existing.setRequiredQty(request.getRequiredQty());
        if (request.getRequiredDate() != null) existing.setRequiredDate(request.getRequiredDate());
        if (request.getMaterialId() != null) existing.setMaterialId(request.getMaterialId());
        if (request.getSiteId() != null) existing.setSiteId(request.getSiteId());
        if (request.getWbsActivityId() != null) existing.setWbsActivityId(request.getWbsActivityId());
        if (request.getZone() != null) existing.setZone(request.getZone());
        if (request.getContractorId() != null) existing.setContractorId(request.getContractorId());
        if (request.getRemarks() != null) existing.setRemarks(request.getRemarks());
        return materialRequestRepository.save(existing);
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
        if (po.getStatus() == null || po.getStatus().isBlank()) {
            po.setStatus("PENDING_APPROVAL");
        } else {
            po.setStatus(po.getStatus().toUpperCase());
        }
        if (po.getPoNumber() == null || po.getPoNumber().isBlank()) {
            po.setPoNumber("PO-" + System.currentTimeMillis());
        }
        return purchaseOrderRepository.save(po);
    }

    @Override
    public PurchaseOrder updatePurchaseOrder(Long id, PurchaseOrder request) {
        PurchaseOrder existing = getPurchaseOrderById(id);
        if (List.of("DELIVERED", "FULFILLED", "CANCELLED").contains(existing.getStatus().toUpperCase())) {
            throw new BadRequestException("Cannot edit Purchase Order in '" + existing.getStatus() + "' status.");
        }
        if (request.getAmount() != null) existing.setAmount(request.getAmount());
        if (request.getDeliveryDate() != null) existing.setDeliveryDate(request.getDeliveryDate());
        if (request.getSupplierId() != null) existing.setSupplierId(request.getSupplierId());
        if (request.getMaterialId() != null) existing.setMaterialId(request.getMaterialId());
        if (request.getOrderQty() != null) existing.setOrderQty(request.getOrderQty());
        if (request.getUnitRate() != null) existing.setUnitRate(request.getUnitRate());
        if (request.getPoNumber() != null) existing.setPoNumber(request.getPoNumber());
        if (request.getRemarks() != null) existing.setRemarks(request.getRemarks());
        return purchaseOrderRepository.save(existing);
    }

    @Override
    public PurchaseOrder updatePurchaseOrderApproval(Long id, com.example.BuildTwin._0.domain.procurement.dto.PurchaseOrderApprovalDto approvalDto) {
        PurchaseOrder existing = getPurchaseOrderById(id);
        String newStatus = approvalDto.getStatus().toUpperCase();
        if (!List.of("APPROVED", "REJECTED", "ISSUED", "PENDING_APPROVAL").contains(newStatus)) {
            throw new BadRequestException("Invalid PO approval status: " + approvalDto.getStatus() + ". Must be APPROVED, REJECTED, or ISSUED.");
        }
        existing.setStatus(newStatus);
        if ("REJECTED".equals(newStatus)) {
            existing.setRejectionReason(approvalDto.getRejectionReason());
        }
        if (approvalDto.getApprovedBy() != null) {
            existing.setApprovedBy(approvalDto.getApprovedBy());
        }
        if (approvalDto.getRemarks() != null) {
            existing.setRemarks(approvalDto.getRemarks());
        }
        return purchaseOrderRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public com.example.BuildTwin._0.domain.procurement.dto.PoFulfillmentDto getPoFulfillmentStatus(Long id) {
        PurchaseOrder po = getPurchaseOrderById(id);
        List<Grn> grns = grnRepository.findByPoId(id);

        BigDecimal totalReceived = BigDecimal.ZERO;
        BigDecimal totalAccepted = BigDecimal.ZERO;
        BigDecimal totalRejected = BigDecimal.ZERO;

        for (Grn grn : grns) {
            if (grn.getReceivedQty() != null) totalReceived = totalReceived.add(grn.getReceivedQty());
            if (grn.getAcceptedQty() != null) totalAccepted = totalAccepted.add(grn.getAcceptedQty());
            if (grn.getRejectedQty() != null) totalRejected = totalRejected.add(grn.getRejectedQty());
        }

        BigDecimal orderQty = po.getOrderQty() != null ? po.getOrderQty() : BigDecimal.ZERO;
        BigDecimal remaining = orderQty.compareTo(totalAccepted) > 0 ? orderQty.subtract(totalAccepted) : BigDecimal.ZERO;

        String percentage = "0.00%";
        if (orderQty.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal pct = totalAccepted.multiply(new BigDecimal("100")).divide(orderQty, 2, java.math.RoundingMode.HALF_UP);
            percentage = pct.toString() + "%";
        }

        return com.example.BuildTwin._0.domain.procurement.dto.PoFulfillmentDto.builder()
                .poId(po.getId())
                .poNumber(po.getPoNumber())
                .supplierId(po.getSupplierId())
                .projectId(po.getProjectId())
                .materialId(po.getMaterialId())
                .orderQty(orderQty)
                .totalReceivedQty(totalReceived)
                .totalAcceptedQty(totalAccepted)
                .totalRejectedQty(totalRejected)
                .remainingQty(remaining)
                .status(po.getStatus())
                .expectedDeliveryDate(po.getDeliveryDate())
                .fulfillmentPercentage(percentage)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrder getPurchaseOrderById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrder> getPurchaseOrdersByProject(Long projectId) {
        return purchaseOrderRepository.findByProjectId(projectId);
    }

    @Override
    public void deletePurchaseOrder(Long id) {
        PurchaseOrder po = getPurchaseOrderById(id);
        purchaseOrderRepository.delete(po);
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

        // Auto-update PO status based on delivery progress
        if (savedGrn.getPoId() != null) {
            purchaseOrderRepository.findById(savedGrn.getPoId()).ifPresent(po -> {
                List<Grn> poGrns = grnRepository.findByPoId(po.getId());
                BigDecimal cumAccepted = poGrns.stream()
                        .map(g -> g.getAcceptedQty() != null ? g.getAcceptedQty() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (po.getOrderQty() != null && po.getOrderQty().compareTo(BigDecimal.ZERO) > 0) {
                    if (cumAccepted.compareTo(po.getOrderQty()) >= 0) {
                        po.setStatus("FULFILLED");
                    } else if (cumAccepted.compareTo(BigDecimal.ZERO) > 0) {
                        po.setStatus("PARTIALLY_DELIVERED");
                    }
                    purchaseOrderRepository.save(po);
                }
            });
        }

        return savedGrn;
    }

    @Override
    @Transactional(readOnly = true)
    public Grn getGrnById(Long id) {
        return grnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grn", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Grn> getGrnsByPo(Long poId) {
        return grnRepository.findByPoId(poId);
    }

    @Override
    public void deleteGrn(Long id) {
        Grn grn = getGrnById(id);
        grnRepository.delete(grn);
    }
}
