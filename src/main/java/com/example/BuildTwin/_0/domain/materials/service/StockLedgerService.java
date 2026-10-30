package com.example.BuildTwin._0.domain.materials.service;

import com.example.BuildTwin._0.domain.materials.enums.StockTransactionType;
import com.example.BuildTwin._0.domain.materials.model.Material;
import com.example.BuildTwin._0.domain.materials.model.StockLedger;
import com.example.BuildTwin._0.domain.materials.repository.MaterialRepository;
import com.example.BuildTwin._0.domain.materials.repository.StockLedgerRepository;
import com.example.BuildTwin._0.domain.materials.dto.StockTransactionDto;
import com.example.BuildTwin._0.domain.materials.exception.InsufficientStockException;
import com.example.BuildTwin._0.domain.notifications.dto.SendNotificationDto;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationPriority;
import com.example.BuildTwin._0.domain.notifications.enums.NotificationType;
import com.example.BuildTwin._0.domain.notifications.service.NotificationService;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.example.BuildTwin._0.domain.materials.dto.StockReconciliationDto;
import com.example.BuildTwin._0.domain.materials.dto.StockReconciliationResultDto;

import java.time.LocalDateTime;

@Slf4j
@Service
public class StockLedgerService {

    private final StockLedgerRepository stockLedgerRepository;
    private final MaterialRepository materialRepository;
    private final Optional<NotificationService> notificationService;

    public StockLedgerService(StockLedgerRepository stockLedgerRepository, MaterialRepository materialRepository) {
        this(stockLedgerRepository, materialRepository, Optional.empty());
    }

    @Autowired
    public StockLedgerService(StockLedgerRepository stockLedgerRepository, MaterialRepository materialRepository, Optional<NotificationService> notificationService) {
        this.stockLedgerRepository = stockLedgerRepository;
        this.materialRepository = materialRepository;
        this.notificationService = notificationService != null ? notificationService : Optional.empty();
    }

    /**
     * Process stock transaction with ACID compliance and pessimistic locking.
     * Guarantees negative stock prevention and immutable audit logging.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public StockLedger recordTransaction(StockTransactionDto dto) {
        // Fetch material with pessimistic lock to prevent concurrent stock race conditions
        Material material = materialRepository.findByIdForUpdate(dto.getMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException("Material", "id", dto.getMaterialId()));

        BigDecimal quantity = dto.getQuantity();
        StockTransactionType txnType = dto.getTransactionType();
        if (txnType == null) {
            throw new com.example.BuildTwin._0.exception.BadRequestException("Transaction type is required");
        }
        BigDecimal currentStock = material.getCurrentStock() != null ? material.getCurrentStock() : BigDecimal.ZERO;

        // Stock ledger calculation logic
        if (txnType == StockTransactionType.ISSUE || txnType == StockTransactionType.CONSUMPTION || txnType == StockTransactionType.WASTAGE || txnType == StockTransactionType.TRANSFER) {
            if (currentStock.compareTo(quantity) < 0) {
                throw new InsufficientStockException(
                        material.getId(),
                        material.getName(),
                        quantity.doubleValue(),
                        currentStock.doubleValue()
                );
            }
            material.setCurrentStock(currentStock.subtract(quantity));
        } else if (txnType == StockTransactionType.RECEIPT || txnType == StockTransactionType.RETURN) {
            material.setCurrentStock(currentStock.add(quantity));
        } else if (txnType == StockTransactionType.ADJUSTMENT) {
            // For stock reconciliation adjustment
            material.setCurrentStock(quantity);
        }

        // Update Material master with new stock level
        materialRepository.save(material);

        // Module 2 Trigger 1: Stock Ledger CONSUMPTION/ISSUE Reorder Level Breach
        if (txnType == StockTransactionType.CONSUMPTION || txnType == StockTransactionType.ISSUE) {
            if (material.getReorderLevel() != null && material.getCurrentStock().compareTo(material.getReorderLevel()) <= 0) {
                notificationService.ifPresent(ns -> {
                    Long projId = dto.getProjectId() != null ? dto.getProjectId() : 1L;
                    if (!ns.isDuplicateActiveAlert(projId, NotificationType.LOW_STOCK, material.getId())) {
                        // Dispatch to PROCUREMENT_STORE (Primary)
                        ns.dispatchNotification(SendNotificationDto.builder()
                                .projectId(projId)
                                .recipientUsername("proc_ramesh")
                                .targetRole("PROCUREMENT_STORE")
                                .title("LOW STOCK ALERT: " + material.getName())
                                .message(String.format("Material '%s' (%s) stock is down to %s %s (Reorder Level: %s). Initiate purchase replenishment.",
                                        material.getName(), material.getMaterialCode(), material.getCurrentStock(), material.getUnit(), material.getReorderLevel()))
                                .notificationType(NotificationType.LOW_STOCK)
                                .priority(NotificationPriority.HIGH)
                                .referenceModule("MATERIALS")
                                .referenceId(material.getId())
                                .build());

                        // Dispatch to SITE_SUPERVISOR (Primary)
                        ns.dispatchNotification(SendNotificationDto.builder()
                                .projectId(projId)
                                .recipientUsername("sup_manoj")
                                .targetRole("SITE_SUPERVISOR")
                                .title("LOW STOCK ALERT: " + material.getName())
                                .message(String.format("Store stock for '%s' is critical at %s %s. Plan upcoming site indents accordingly.",
                                        material.getName(), material.getCurrentStock(), material.getUnit()))
                                .notificationType(NotificationType.LOW_STOCK)
                                .priority(NotificationPriority.HIGH)
                                .referenceModule("MATERIALS")
                                .referenceId(material.getId())
                                .build());

                        log.info("Dispatched LOW_STOCK alerts to PROCUREMENT_STORE & SITE_SUPERVISOR for material '{}'", material.getName());
                    } else {
                        log.debug("Active unread LOW_STOCK alert already exists within 24 hours for material ID: {}. Deduplication suppressed duplicate alert.", material.getId());
                    }
                });
            }
        }

        // Create immutable stock ledger audit entry
        StockLedger entry = StockLedger.builder()
                .projectId(dto.getProjectId())
                .siteId(dto.getSiteId())
                .activityId(dto.getActivityId())
                .zone(dto.getZone())
                .contractorId(dto.getContractorId())
                .material(material)
                .transactionType(txnType)
                .quantity(quantity)
                .unitPrice(dto.getUnitPrice() != null ? dto.getUnitPrice() : material.getStandardRate())
                .referenceId(dto.getReferenceId())
                .remarks(dto.getRemarks())
                .build();

        return stockLedgerRepository.save(entry);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public StockLedger recordReceipt(StockTransactionDto dto) {
        dto.setTransactionType(StockTransactionType.RECEIPT);
        return recordTransaction(dto);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public StockLedger issueMaterial(StockTransactionDto dto) {
        dto.setTransactionType(StockTransactionType.ISSUE);
        return recordTransaction(dto);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public StockLedger recordConsumption(StockTransactionDto dto) {
        dto.setTransactionType(StockTransactionType.CONSUMPTION);
        return recordTransaction(dto);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public StockLedger returnMaterial(StockTransactionDto dto) {
        dto.setTransactionType(StockTransactionType.RETURN);
        return recordTransaction(dto);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public StockLedger recordWastage(StockTransactionDto dto) {
        dto.setTransactionType(StockTransactionType.WASTAGE);
        return recordTransaction(dto);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public StockLedger transferMaterial(StockTransactionDto dto) {
        dto.setTransactionType(StockTransactionType.TRANSFER);
        return recordTransaction(dto);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public StockReconciliationResultDto reconcileStock(StockReconciliationDto dto) {
        Material material = materialRepository.findByIdForUpdate(dto.getMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException("Material", "id", dto.getMaterialId()));

        BigDecimal systemStock = material.getCurrentStock() != null ? material.getCurrentStock() : BigDecimal.ZERO;
        BigDecimal physicalStock = dto.getPhysicalQty();
        BigDecimal variance = physicalStock.subtract(systemStock);

        // Update material current stock to physical count
        material.setCurrentStock(physicalStock);
        materialRepository.save(material);

        String auditor = dto.getAuditedBy() != null ? dto.getAuditedBy() : "Auditor";
        String statusStr = variance.compareTo(BigDecimal.ZERO) == 0 ? "MATCHED" :
                (variance.compareTo(BigDecimal.ZERO) > 0 ? "SURPLUS" : "SHORTAGE");

        // Create adjustment entry in stock ledger
        String remarkText = "Stock Reconciliation Audit by " + auditor
                + ". Status: " + statusStr
                + ". Variance: " + variance
                + (dto.getRemarks() != null ? " - " + dto.getRemarks() : "");

        BigDecimal txnQuantity = physicalStock.compareTo(BigDecimal.ZERO) > 0 ? physicalStock :
                (variance.abs().compareTo(BigDecimal.ZERO) > 0 ? variance.abs() : BigDecimal.ONE);

        StockLedger ledgerEntry = StockLedger.builder()
                .projectId(dto.getProjectId())
                .siteId(dto.getSiteId())
                .material(material)
                .transactionType(StockTransactionType.ADJUSTMENT)
                .quantity(txnQuantity)
                .unitPrice(material.getStandardRate())
                .referenceId("RECON-" + System.currentTimeMillis())
                .remarks(remarkText)
                .build();

        StockLedger savedEntry = stockLedgerRepository.save(ledgerEntry);

        return StockReconciliationResultDto.builder()
                .materialId(material.getId())
                .materialCode(material.getMaterialCode())
                .materialName(material.getName())
                .unit(material.getUnit() != null ? material.getUnit().name() : null)
                .systemStock(systemStock)
                .physicalStock(physicalStock)
                .variance(variance)
                .status(statusStr)
                .remarks(dto.getRemarks())
                .adjustmentTransactionId(savedEntry.getId())
                .auditedBy(auditor)
                .reconciledAt(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public List<StockLedger> getLedgerEntriesByMaterial(Long materialId) {
        return getLedgerEntriesByMaterial(materialId, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<StockLedger> getLedgerEntriesByMaterial(Long materialId, Long projectId, LocalDateTime startDate, LocalDateTime endDate) {
        if (projectId != null && startDate != null && endDate != null) {
            return stockLedgerRepository.findByMaterialIdAndProjectIdAndTimestampBetweenOrderByTimestampAsc(materialId, projectId, startDate, endDate);
        } else if (projectId != null) {
            return stockLedgerRepository.findByMaterialIdAndProjectIdOrderByTimestampAsc(materialId, projectId);
        } else if (startDate != null && endDate != null) {
            return stockLedgerRepository.findByMaterialIdAndTimestampBetweenOrderByTimestampAsc(materialId, startDate, endDate);
        } else if (startDate != null) {
            return stockLedgerRepository.findByMaterialIdAndTimestampBetweenOrderByTimestampAsc(materialId, startDate, LocalDateTime.now());
        } else {
            return stockLedgerRepository.findByMaterialIdOrderByTimestampAsc(materialId);
        }
    }

    @Transactional(readOnly = true)
    public List<StockLedger> getLedgerEntriesByProject(Long projectId) {
        return stockLedgerRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public List<StockLedger> getLedgerEntriesByActivity(Long activityId) {
        return stockLedgerRepository.findByActivityId(activityId);
    }
}
