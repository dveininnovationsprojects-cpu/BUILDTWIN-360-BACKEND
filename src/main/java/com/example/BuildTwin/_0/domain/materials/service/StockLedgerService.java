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
        if (txnType == StockTransactionType.ISSUE || txnType == StockTransactionType.CONSUMPTION) {
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
                .material(material)
                .transactionType(txnType)
                .quantity(quantity)
                .unitPrice(dto.getUnitPrice() != null ? dto.getUnitPrice() : material.getStandardRate())
                .referenceId(dto.getReferenceId())
                .remarks(dto.getRemarks())
                .build();

        return stockLedgerRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<StockLedger> getLedgerEntriesByMaterial(Long materialId) {
        return stockLedgerRepository.findByMaterialId(materialId);
    }

    @Transactional(readOnly = true)
    public List<StockLedger> getLedgerEntriesByProject(Long projectId) {
        return stockLedgerRepository.findByProjectId(projectId);
    }
}
