package com.example.BuildTwin._0.domain.materials.service;

import com.example.BuildTwin._0.domain.materials.enums.StockTransactionType;
import com.example.BuildTwin._0.domain.materials.model.Material;
import com.example.BuildTwin._0.domain.materials.model.StockLedger;
import com.example.BuildTwin._0.domain.materials.repository.MaterialRepository;
import com.example.BuildTwin._0.domain.materials.repository.StockLedgerRepository;
import com.example.BuildTwin._0.domain.materials.dto.StockTransactionDto;
import com.example.BuildTwin._0.domain.materials.exception.InsufficientStockException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import com.example.BuildTwin._0.domain.materials.dto.StockReconciliationDto;
import com.example.BuildTwin._0.domain.materials.dto.StockReconciliationResultDto;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StockLedgerService {

    private final StockLedgerRepository stockLedgerRepository;
    private final MaterialRepository materialRepository;

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
        BigDecimal currentStock = material.getCurrentStock() != null ? material.getCurrentStock() : BigDecimal.ZERO;

        // Stock ledger calculation logic
        if (txnType == StockTransactionType.ISSUE || txnType == StockTransactionType.CONSUMPTION || txnType == StockTransactionType.WASTAGE) {
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
    public StockLedger recordWastage(StockTransactionDto dto) {
        dto.setTransactionType(StockTransactionType.WASTAGE);
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

        // Create adjustment entry in stock ledger
        String remarkText = "Stock Reconciliation Audit by " + (dto.getAuditedBy() != null ? dto.getAuditedBy() : "Auditor")
                + ". Variance: " + variance
                + (dto.getRemarks() != null ? " - " + dto.getRemarks() : "");

        StockLedger ledgerEntry = StockLedger.builder()
                .projectId(dto.getProjectId())
                .siteId(dto.getSiteId())
                .material(material)
                .transactionType(StockTransactionType.ADJUSTMENT)
                .quantity(physicalStock)
                .unitPrice(material.getStandardRate())
                .referenceId("RECON-" + System.currentTimeMillis())
                .remarks(remarkText)
                .build();

        StockLedger savedEntry = stockLedgerRepository.save(ledgerEntry);

        return StockReconciliationResultDto.builder()
                .materialId(material.getId())
                .materialCode(material.getMaterialCode())
                .materialName(material.getName())
                .systemStock(systemStock)
                .physicalStock(physicalStock)
                .variance(variance)
                .adjustmentTransactionId(savedEntry.getId())
                .auditedBy(dto.getAuditedBy())
                .reconciledAt(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public List<StockLedger> getLedgerEntriesByMaterial(Long materialId) {
        return stockLedgerRepository.findByMaterialId(materialId);
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
