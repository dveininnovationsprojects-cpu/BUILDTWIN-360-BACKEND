package com.example.BuildTwin._0.domain.cost.service;

import com.example.BuildTwin._0.domain.cost.dto.*;
import com.example.BuildTwin._0.domain.cost.enums.CostHead;
import com.example.BuildTwin._0.domain.cost.enums.CostSourceType;
import com.example.BuildTwin._0.domain.cost.model.Budget;
import com.example.BuildTwin._0.domain.cost.model.CostActualLedger;
import com.example.BuildTwin._0.domain.cost.model.CostTransaction;
import com.example.BuildTwin._0.domain.cost.model.ProjectBudget;
import com.example.BuildTwin._0.domain.cost.model.ProjectEVMRecord;
import com.example.BuildTwin._0.domain.cost.repository.BudgetRepository;
import com.example.BuildTwin._0.domain.cost.repository.CostActualLedgerRepository;
import com.example.BuildTwin._0.domain.cost.repository.CostTransactionRepository;
import com.example.BuildTwin._0.domain.cost.repository.ProjectBudgetRepository;
import com.example.BuildTwin._0.domain.cost.repository.ProjectEVMRecordRepository;
import com.example.BuildTwin._0.domain.labour.model.LabourDaily;
import com.example.BuildTwin._0.domain.labour.repository.LabourDailyRepository;
import com.example.BuildTwin._0.domain.materials.model.StockLedger;
import com.example.BuildTwin._0.domain.materials.repository.StockLedgerRepository;
import com.example.BuildTwin._0.domain.procurement.model.Grn;
import com.example.BuildTwin._0.domain.procurement.model.PurchaseOrder;
import com.example.BuildTwin._0.domain.procurement.repository.GrnRepository;
import com.example.BuildTwin._0.domain.procurement.repository.PurchaseOrderRepository;
import com.example.BuildTwin._0.domain.projects.repository.ProjectRepository;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CostServiceImpl implements CostService {

    private final BudgetRepository budgetRepository;
    private final CostTransactionRepository costTransactionRepository;
    private final StockLedgerRepository stockLedgerRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProjectRepository projectRepository;
    private final LabourDailyRepository labourDailyRepository;
    private final GrnRepository grnRepository;
    private final ProjectBudgetRepository projectBudgetRepository;
    private final CostActualLedgerRepository costActualLedgerRepository;
    private final ProjectEVMRecordRepository projectEVMRecordRepository;
    private final com.example.BuildTwin._0.domain.equipment.repository.EquipmentUsageRepository equipmentUsageRepository;

    @org.springframework.beans.factory.annotation.Value("${app.cost.rates.equipment.hourly-default:1200.00}")
    private BigDecimal defaultEquipmentHourlyRate = new BigDecimal("1200.00");

    @org.springframework.beans.factory.annotation.Value("${app.cost.rates.labour.mason:200.00}")
    private BigDecimal masonHourlyRate = new BigDecimal("200.00");

    @org.springframework.beans.factory.annotation.Value("${app.cost.rates.labour.carpenter:220.00}")
    private BigDecimal carpenterHourlyRate = new BigDecimal("220.00");

    @org.springframework.beans.factory.annotation.Value("${app.cost.rates.labour.electrician:250.00}")
    private BigDecimal electricianHourlyRate = new BigDecimal("250.00");

    @org.springframework.beans.factory.annotation.Value("${app.cost.rates.labour.helper:120.00}")
    private BigDecimal helperHourlyRate = new BigDecimal("120.00");

    @org.springframework.beans.factory.annotation.Value("${app.cost.rates.labour.default:180.00}")
    private BigDecimal defaultLabourHourlyRate = new BigDecimal("180.00");

    private BigDecimal getTradeRate(com.example.BuildTwin._0.domain.labour.enums.TradeCategory trade) {
        if (trade == null) return defaultLabourHourlyRate;
        return switch (trade) {
            case MASON -> masonHourlyRate;
            case CARPENTER, BAR_BENDER, WELDER -> carpenterHourlyRate;
            case ELECTRICIAN, PLUMBER -> electricianHourlyRate;
            case HELPER -> helperHourlyRate;
            default -> defaultLabourHourlyRate;
        };
    }

    // ==========================================
    // Day 1: Project Cost Structure Setup (CBS)
    // ==========================================

    @Override
    public Budget createOrUpdateBudget(BudgetRequestDto dto) {
        Budget result = budgetRepository.findByProjectIdAndCostCode(dto.getProjectId(), dto.getCostCode())
                .map(existing -> {
                    if (dto.getBaselineAmount() != null) {
                        existing.setBaselineAmount(dto.getBaselineAmount());
                    }
                    if (dto.getRevisedAmount() != null) {
                        existing.setRevisedAmount(dto.getRevisedAmount());
                    }
                    if (dto.getActivityId() != null) {
                        existing.setActivityId(dto.getActivityId());
                        existing.setWbsActivityId(dto.getActivityId());
                    }
                    if (dto.getCostHead() != null) {
                        existing.setCostHead(dto.getCostHead());
                    }
                    if (dto.getDescription() != null) {
                        existing.setDescription(dto.getDescription());
                    }
                    if (dto.getRevisionReason() != null) {
                        existing.setRevisionReason(dto.getRevisionReason());
                    }
                    if (dto.getApprovedBy() != null) {
                        existing.setApprovedBy(dto.getApprovedBy());
                    }
                    if (dto.getCurrency() != null) {
                        existing.setCurrency(dto.getCurrency());
                    }
                    Budget saved = budgetRepository.save(existing);
                    log.info("Updated existing CBS Budget Head: {} (ID: {}) for Project: {} | Baseline: {}, Revised: {}",
                            saved.getCostCode(), saved.getId(), saved.getProjectId(), saved.getBaselineAmount(), saved.getRevisedAmount());
                    return saved;
                })
                .orElseGet(() -> {
                    Budget created = budgetRepository.save(Budget.builder()
                            .projectId(dto.getProjectId())
                            .activityId(dto.getActivityId())
                            .wbsActivityId(dto.getActivityId())
                            .costCode(dto.getCostCode())
                            .costHead(dto.getCostHead())
                            .description(dto.getDescription())
                            .baselineAmount(dto.getBaselineAmount())
                            .revisedAmount(dto.getRevisedAmount() != null ? dto.getRevisedAmount() : BigDecimal.ZERO)
                            .currency(dto.getCurrency() != null ? dto.getCurrency() : "INR")
                            .revisionReason(dto.getRevisionReason())
                            .approvedBy(dto.getApprovedBy())
                            .status("APPROVED")
                            .build());
                    log.info("Created new CBS Budget Head: {} (ID: {}) for Project: {} | Baseline: {}",
                            created.getCostCode(), created.getId(), created.getProjectId(), created.getBaselineAmount());
                    return created;
                });

        try {
            ProjectBudget pb = projectBudgetRepository.findByProjectIdAndCostCode(dto.getProjectId(), dto.getCostCode())
                    .orElse(ProjectBudget.builder()
                            .projectId(dto.getProjectId())
                            .wbsActivityId(dto.getActivityId())
                            .costCode(dto.getCostCode())
                            .allocatedAmount(dto.getBaselineAmount())
                            .revisedAmount(dto.getRevisedAmount() != null ? dto.getRevisedAmount() : BigDecimal.ZERO)
                            .currency(dto.getCurrency() != null ? dto.getCurrency() : "INR")
                            .description(dto.getDescription())
                            .build());
            pb.setAllocatedAmount(dto.getBaselineAmount());
            if (dto.getRevisedAmount() != null) pb.setRevisedAmount(dto.getRevisedAmount());
            if (dto.getActivityId() != null) pb.setWbsActivityId(dto.getActivityId());
            if (dto.getDescription() != null) pb.setDescription(dto.getDescription());
            projectBudgetRepository.save(pb);
        } catch (Exception e) {
            log.warn("ProjectBudget mirror sync exception: {}", e.getMessage());
        }

        return result;
    }

    @Override
    public Budget updateBudget(Long id, BudgetRequestDto dto) {
        Budget existing = getBudgetById(id);
        if (dto.getCostCode() != null) existing.setCostCode(dto.getCostCode());
        if (dto.getCostHead() != null) existing.setCostHead(dto.getCostHead());
        if (dto.getActivityId() != null) existing.setActivityId(dto.getActivityId());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getBaselineAmount() != null) existing.setBaselineAmount(dto.getBaselineAmount());
        if (dto.getRevisedAmount() != null) existing.setRevisedAmount(dto.getRevisedAmount());
        if (dto.getRevisionReason() != null) existing.setRevisionReason(dto.getRevisionReason());
        if (dto.getApprovedBy() != null) existing.setApprovedBy(dto.getApprovedBy());

        Budget saved = budgetRepository.save(existing);
        log.info("Updated Budget ID: {} ({}) for Project: {} | Baseline: {}, Revised: {}",
                id, saved.getCostCode(), saved.getProjectId(), saved.getBaselineAmount(), saved.getRevisedAmount());
        return saved;
    }

    @Override
    public Budget authorizeBudgetRevision(Long id, BudgetRevisionDto dto) {
        Budget existing = getBudgetById(id);
        existing.setRevisedAmount(dto.getRevisedAmount());
        existing.setRevisionReason(dto.getRevisionReason());
        if (dto.getApprovedBy() != null) {
            existing.setApprovedBy(dto.getApprovedBy());
        }
        existing.setStatus("APPROVED_REVISED");

        Budget saved = budgetRepository.save(existing);
        log.info("Authorized Budget Revision for Budget ID: {} (Cost Code: {}) | Revised Amount: {} | Reason: '{}' | ApprovedBy: {}",
                id, saved.getCostCode(), saved.getRevisedAmount(), saved.getRevisionReason(), saved.getApprovedBy());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Budget getBudgetById(Long id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Budget> getBudgetsByProject(Long projectId) {
        return budgetRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Budget> getBudgetsByProjectAndActivity(Long projectId, Long activityId) {
        return budgetRepository.findByProjectIdAndActivityId(projectId, activityId);
    }

    @Override
    public void deleteBudget(Long id) {
        Budget budget = getBudgetById(id);
        budgetRepository.delete(budget);
        log.warn("Deleted Budget ID: {} (Cost Code: {}) for Project: {}", id, budget.getCostCode(), budget.getProjectId());
    }

    // ==========================================
    // Cost Transactions & Ledger
    // ==========================================

    @Override
    public CostTransaction recordCostTransaction(CostTransactionRequestDto dto) {
        CostTransaction tx = CostTransaction.builder()
                .projectId(dto.getProjectId())
                .activityId(dto.getActivityId())
                .costCode(dto.getCostCode())
                .costHead(dto.getCostHead())
                .sourceType(dto.getSourceType())
                .amount(dto.getAmount())
                .transactionDate(dto.getTransactionDate())
                .referenceNumber(dto.getReferenceNumber())
                .vendorOrPayee(dto.getVendorOrPayee())
                .remarks(dto.getRemarks())
                .build();
        CostTransaction saved = costTransactionRepository.save(tx);
        log.info("Recorded Cost Transaction ID: {} | Project: {}, Code: {}, Head: {}, Source: {}, Amount: {}, Ref: '{}'",
                saved.getId(), saved.getProjectId(), saved.getCostCode(), saved.getCostHead(), saved.getSourceType(), saved.getAmount(), saved.getReferenceNumber());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public CostTransaction getCostTransactionById(Long id) {
        return costTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CostTransaction not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CostTransaction> getAllCostTransactions() {
        return costTransactionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CostTransaction> getCostTransactionsByProject(Long projectId) {
        return costTransactionRepository.findByProjectId(projectId);
    }

    @Override
    public void deleteCostTransaction(Long id) {
        CostTransaction tx = getCostTransactionById(id);
        costTransactionRepository.delete(tx);
        log.warn("Deleted Cost Transaction ID: {} | Amount: {} for Project: {}", id, tx.getAmount(), tx.getProjectId());
    }

    // ==========================================
    // Day 2: Material Cost + Labour Cost Tracking
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public MaterialActualCostSummaryDto getMaterialActualCostSummary(Long projectId) {
        List<StockLedger> consumptionEntries = stockLedgerRepository.findByProjectId(projectId).stream()
                .filter(s -> s.getTransactionType() != null && "CONSUMPTION".equalsIgnoreCase(s.getTransactionType().name()))
                .toList();

        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalQty = BigDecimal.ZERO;
        List<MaterialActualCostSummaryDto.MaterialCostItemDto> items = new ArrayList<>();

        for (StockLedger entry : consumptionEntries) {
            BigDecimal qty = entry.getQuantity() != null ? entry.getQuantity() : BigDecimal.ZERO;
            BigDecimal price = entry.getUnitPrice() != null ? entry.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal lineCost = qty.multiply(price);

            totalQty = totalQty.add(qty);
            totalCost = totalCost.add(lineCost);

            items.add(MaterialActualCostSummaryDto.MaterialCostItemDto.builder()
                    .materialId(entry.getMaterial() != null ? entry.getMaterial().getId() : null)
                    .materialCode(entry.getMaterial() != null ? entry.getMaterial().getMaterialCode() : "N/A")
                    .materialName(entry.getMaterial() != null ? entry.getMaterial().getName() : "Material #" + entry.getId())
                    .quantity(qty)
                    .unitPrice(price)
                    .totalCost(lineCost)
                    .referenceId(entry.getReferenceId())
                    .timestamp(entry.getTimestamp() != null ? entry.getTimestamp().toString() : null)
                    .build());
        }

        // If no stock ledger consumption, fallback to cost_transactions with MATERIAL costHead
        if (items.isEmpty()) {
            List<CostTransaction> matTxns = costTransactionRepository.findByProjectIdAndCostHead(projectId, CostHead.MATERIAL);
            for (CostTransaction tx : matTxns) {
                BigDecimal amt = tx.getAmount() != null ? tx.getAmount() : BigDecimal.ZERO;
                totalCost = totalCost.add(amt);
                items.add(MaterialActualCostSummaryDto.MaterialCostItemDto.builder()
                        .materialCode(tx.getCostCode())
                        .materialName("Material Expense")
                        .quantity(BigDecimal.ZERO)
                        .unitPrice(amt)
                        .totalCost(amt)
                        .referenceId(tx.getReferenceNumber())
                        .timestamp(tx.getTransactionDate() != null ? tx.getTransactionDate().toString() : null)
                        .build());
            }
        }

        log.debug("Fetched Material Actual Cost Summary for Project ID: {} | Total Cost: {}, Qty: {}", projectId, totalCost, totalQty);

        return MaterialActualCostSummaryDto.builder()
                .projectId(projectId)
                .totalMaterialCost(totalCost)
                .totalQuantityConsumed(totalQty)
                .entriesCount(items.size())
                .items(items)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LabourActualCostSummaryDto getLabourActualCostSummary(Long projectId) {
        List<LabourDaily> dailyRecords = labourDailyRepository.findByProjectId(projectId);

        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalManHours = BigDecimal.ZERO;
        int totalHeadcount = 0;
        List<LabourActualCostSummaryDto.LabourCostItemDto> items = new ArrayList<>();

        for (LabourDaily rec : dailyRecords) {
            int hc = rec.getHeadcount() != null ? rec.getHeadcount() : 1;
            BigDecimal stdHrs = rec.getStandardHours() != null ? rec.getStandardHours() : new BigDecimal("8.00");
            BigDecimal otHrs = rec.getOvertimeHours() != null ? rec.getOvertimeHours() : BigDecimal.ZERO;
            BigDecimal totalHrs = stdHrs.add(otHrs).multiply(BigDecimal.valueOf(hc));

            // Dynamic benchmark hourly wage rates per trade (configurable via application.properties)
            BigDecimal baseRate = getTradeRate(rec.getTradeCategory());

            BigDecimal stdCost = stdHrs.multiply(BigDecimal.valueOf(hc)).multiply(baseRate);
            BigDecimal otCost = otHrs.multiply(BigDecimal.valueOf(hc)).multiply(baseRate).multiply(new BigDecimal("1.50"));
            BigDecimal lineCost = stdCost.add(otCost);

            totalCost = totalCost.add(lineCost);
            totalManHours = totalManHours.add(totalHrs);
            totalHeadcount += hc;

            items.add(LabourActualCostSummaryDto.LabourCostItemDto.builder()
                    .tradeCategory(rec.getTradeCategory() != null ? rec.getTradeCategory().name() : "GENERAL")
                    .contractorName(rec.getContractor() != null ? rec.getContractor().getName() : "Direct Labor")
                    .headcount(hc)
                    .standardHours(stdHrs)
                    .overtimeHours(otHrs)
                    .totalHours(totalHrs)
                    .hourlyRate(baseRate)
                    .totalCost(lineCost)
                    .recordDate(rec.getRecordDate() != null ? rec.getRecordDate().toString() : null)
                    .build());
        }

        // If no daily labour records, fallback to cost_transactions with LABOUR costHead
        if (items.isEmpty()) {
            List<CostTransaction> labTxns = costTransactionRepository.findByProjectIdAndCostHead(projectId, CostHead.LABOUR);
            for (CostTransaction tx : labTxns) {
                BigDecimal amt = tx.getAmount() != null ? tx.getAmount() : BigDecimal.ZERO;
                totalCost = totalCost.add(amt);
                items.add(LabourActualCostSummaryDto.LabourCostItemDto.builder()
                        .tradeCategory("LABOUR")
                        .contractorName(tx.getVendorOrPayee() != null ? tx.getVendorOrPayee() : "Subcontractor Labour")
                        .headcount(0)
                        .standardHours(BigDecimal.ZERO)
                        .overtimeHours(BigDecimal.ZERO)
                        .totalHours(BigDecimal.ZERO)
                        .hourlyRate(BigDecimal.ZERO)
                        .totalCost(amt)
                        .recordDate(tx.getTransactionDate() != null ? tx.getTransactionDate().toString() : null)
                        .build());
            }
        }

        log.debug("Fetched Labour Actual Cost Summary for Project ID: {} | Total Cost: {}, Hours: {}", projectId, totalCost, totalManHours);

        return LabourActualCostSummaryDto.builder()
                .projectId(projectId)
                .totalLabourCost(totalCost)
                .totalManHours(totalManHours)
                .totalHeadcount(totalHeadcount)
                .items(items)
                .build();
    }

    @Override
    public CostSyncResultDto syncActualCosts(Long projectId) {
        int matCount = 0;
        int labCount = 0;
        BigDecimal totalSynced = BigDecimal.ZERO;

        // 1. Sync Stock Ledger CONSUMPTION entries into cost_transactions
        List<StockLedger> consumptions = stockLedgerRepository.findByProjectId(projectId).stream()
                .filter(s -> s.getTransactionType() != null && "CONSUMPTION".equalsIgnoreCase(s.getTransactionType().name()))
                .toList();

        for (StockLedger entry : consumptions) {
            String ref = "STK-CONS-" + entry.getId();
            boolean exists = costTransactionRepository.findByProjectId(projectId).stream()
                    .anyMatch(t -> ref.equals(t.getReferenceNumber()));
            if (!exists) {
                BigDecimal qty = entry.getQuantity() != null ? entry.getQuantity() : BigDecimal.ZERO;
                BigDecimal price = entry.getUnitPrice() != null ? entry.getUnitPrice() : BigDecimal.ZERO;
                BigDecimal cost = qty.multiply(price).setScale(2, RoundingMode.HALF_UP);

                if (cost.compareTo(BigDecimal.ZERO) > 0) {
                    costTransactionRepository.save(CostTransaction.builder()
                            .projectId(projectId)
                            .activityId(entry.getActivityId())
                            .costCode("MATERIAL_CONSUMPTION")
                            .costHead(CostHead.MATERIAL)
                            .sourceType(CostSourceType.ACTUAL_EXPENSE)
                            .amount(cost)
                            .transactionDate(entry.getTimestamp() != null ? entry.getTimestamp().toLocalDate() : LocalDate.now())
                            .referenceNumber(ref)
                            .remarks("Auto-synced from Stock Ledger consumption #" + entry.getId())
                            .build());
                    matCount++;
                    totalSynced = totalSynced.add(cost);
                }
            }
        }

        // 2. Sync Labour Daily records into cost_transactions
        List<LabourDaily> labourRecords = labourDailyRepository.findByProjectId(projectId);
        for (LabourDaily rec : labourRecords) {
            String ref = "LAB-REC-" + rec.getId();
            boolean exists = costTransactionRepository.findByProjectId(projectId).stream()
                    .anyMatch(t -> ref.equals(t.getReferenceNumber()));
            if (!exists) {
                int hc = rec.getHeadcount() != null ? rec.getHeadcount() : 1;
                BigDecimal stdHrs = rec.getStandardHours() != null ? rec.getStandardHours() : new BigDecimal("8.00");
                BigDecimal otHrs = rec.getOvertimeHours() != null ? rec.getOvertimeHours() : BigDecimal.ZERO;
                BigDecimal rate = getTradeRate(rec.getTradeCategory());
                BigDecimal cost = stdHrs.multiply(BigDecimal.valueOf(hc)).multiply(rate)
                        .add(otHrs.multiply(BigDecimal.valueOf(hc)).multiply(rate).multiply(new BigDecimal("1.50")))
                        .setScale(2, RoundingMode.HALF_UP);

                if (cost.compareTo(BigDecimal.ZERO) > 0) {
                    costTransactionRepository.save(CostTransaction.builder()
                            .projectId(projectId)
                            .costCode("LABOUR_WAGES")
                            .costHead(CostHead.LABOUR)
                            .sourceType(CostSourceType.LABOUR_WAGES)
                            .amount(cost)
                            .transactionDate(rec.getRecordDate() != null ? rec.getRecordDate() : LocalDate.now())
                            .referenceNumber(ref)
                            .vendorOrPayee(rec.getContractor() != null ? rec.getContractor().getName() : "Direct Labour")
                            .remarks("Auto-synced from Daily Labour Record #" + rec.getId() + " (" + rec.getTradeCategory() + ")")
                            .build());
                    labCount++;
                    totalSynced = totalSynced.add(cost);
                }
            }
        }

        totalSynced = totalSynced.setScale(2, RoundingMode.HALF_UP);

        log.info("Synchronized ground consumptions into Cost Ledger for Project ID: {} | Materials: {}, Labour: {}, Synced Cost: {}",
                projectId, matCount, labCount, totalSynced);

        return CostSyncResultDto.builder()
                .projectId(projectId)
                .materialTransactionsSynced(matCount)
                .labourTransactionsSynced(labCount)
                .totalSyncedAmount(totalSynced)
                .message("Stock & Labour consumption successfully synchronized into central Cost Ledger")
                .build();
    }

    // ==========================================
    // Day 3: Procurement Commitments & Costing
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public ProcurementCommitmentDto getProcurementCommitments(Long projectId) {
        List<PurchaseOrder> pos = purchaseOrderRepository.findByProjectId(projectId);

        BigDecimal committed = BigDecimal.ZERO;
        BigDecimal fulfilled = BigDecimal.ZERO;
        BigDecimal cancelled = BigDecimal.ZERO;
        List<PoCommitmentItemDto> openOrders = new ArrayList<>();

        for (PurchaseOrder po : pos) {
            String status = po.getStatus() != null ? po.getStatus().toUpperCase() : "DRAFT";
            BigDecimal amt = po.getAmount() != null ? po.getAmount() : BigDecimal.ZERO;

            if (List.of("ISSUED", "PARTIALLY_FULFILLED", "APPROVED", "DRAFT").contains(status)) {
                committed = committed.add(amt);
                openOrders.add(PoCommitmentItemDto.builder()
                        .poId(po.getId())
                        .poNumber(po.getPoNumber())
                        .supplierId(po.getSupplierId())
                        .materialId(po.getMaterialId())
                        .orderQty(po.getOrderQty())
                        .unitRate(po.getUnitRate())
                        .amount(amt)
                        .status(status)
                        .expectedDeliveryDate(po.getDeliveryDate())
                        .build());
            } else if (List.of("FULFILLED", "DELIVERED").contains(status)) {
                fulfilled = fulfilled.add(amt);
            } else if ("CANCELLED".equals(status)) {
                cancelled = cancelled.add(amt);
            }
        }

        log.debug("Computed Procurement Commitments for Project ID: {} | Open: {}, Fulfilled: {}, Count: {}",
                projectId, committed, fulfilled, openOrders.size());

        return ProcurementCommitmentDto.builder()
                .projectId(projectId)
                .totalCommittedAmount(committed)
                .totalFulfilledAmount(fulfilled)
                .totalCancelledAmount(cancelled)
                .activeCommitmentBalance(committed)
                .openOrdersCount(openOrders.size())
                .openOrders(openOrders)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProcurementExpenditureTrailDto getProcurementExpenditureTrail(Long projectId) {
        List<PurchaseOrder> pos = purchaseOrderRepository.findByProjectId(projectId);
        List<Grn> grns = grnRepository.findByProjectId(projectId);

        BigDecimal committedCost = BigDecimal.ZERO;
        BigDecimal incurredCost = BigDecimal.ZERO;
        BigDecimal fulfilledBilling = BigDecimal.ZERO;

        List<PoCommitmentItemDto> openOrders = new ArrayList<>();

        for (PurchaseOrder po : pos) {
            String status = po.getStatus() != null ? po.getStatus().toUpperCase() : "DRAFT";
            BigDecimal amt = po.getAmount() != null ? po.getAmount() : BigDecimal.ZERO;

            if ("ISSUED".equalsIgnoreCase(status) || "PARTIALLY_FULFILLED".equalsIgnoreCase(status)) {
                committedCost = committedCost.add(amt);
                openOrders.add(PoCommitmentItemDto.builder()
                        .poId(po.getId())
                        .poNumber(po.getPoNumber())
                        .supplierId(po.getSupplierId())
                        .materialId(po.getMaterialId())
                        .orderQty(po.getOrderQty())
                        .unitRate(po.getUnitRate())
                        .amount(amt)
                        .status(status)
                        .expectedDeliveryDate(po.getDeliveryDate())
                        .build());
            } else if ("FULFILLED".equalsIgnoreCase(status) || "DELIVERED".equalsIgnoreCase(status)) {
                fulfilledBilling = fulfilledBilling.add(amt);
            }
        }

        // Incurred cost calculated from accepted GRNs bill value
        for (Grn grn : grns) {
            if (!"VOID".equalsIgnoreCase(grn.getStatus())) {
                BigDecimal accepted = grn.getAcceptedQty() != null ? grn.getAcceptedQty() : BigDecimal.ZERO;
                BigDecimal unitRate = BigDecimal.ZERO;
                if (grn.getPoId() != null) {
                    unitRate = purchaseOrderRepository.findById(grn.getPoId())
                            .map(p -> p.getUnitRate() != null ? p.getUnitRate() : BigDecimal.ZERO)
                            .orElse(BigDecimal.ZERO);
                }
                incurredCost = incurredCost.add(accepted.multiply(unitRate));
            }
        }

        committedCost = committedCost.setScale(2, RoundingMode.HALF_UP);
        incurredCost = incurredCost.setScale(2, RoundingMode.HALF_UP);
        fulfilledBilling = fulfilledBilling.setScale(2, RoundingMode.HALF_UP);
        BigDecimal outstandingLiability = committedCost.subtract(incurredCost).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        log.debug("Generated Procurement Expenditure Trail for Project ID: {} | Committed: {}, Incurred: {}",
                projectId, committedCost, incurredCost);

        return ProcurementExpenditureTrailDto.builder()
                .projectId(projectId)
                .committedCost(committedCost)
                .incurredCost(incurredCost)
                .totalFulfilledBilling(fulfilledBilling)
                .outstandingLiability(outstandingLiability)
                .totalPoCount(pos.size())
                .totalGrnCount(grns.size())
                .openCommitments(openOrders)
                .build();
    }

    // ==========================================
    // Day 4: Baseline Budget vs Actual Expense
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public BudgetVarianceReportDto getBudgetVarianceReport(Long projectId) {
        String projectName = projectRepository.findById(projectId)
                .map(p -> p.getName())
                .orElse("Project #" + projectId);

        List<Budget> budgets = budgetRepository.findByProjectId(projectId);
        List<CostTransaction> transactions = costTransactionRepository.findByProjectId(projectId);
        List<PurchaseOrder> pos = purchaseOrderRepository.findByProjectId(projectId);

        BigDecimal totalBaseline = BigDecimal.ZERO;
        BigDecimal totalRevised = BigDecimal.ZERO;
        BigDecimal totalActual = BigDecimal.ZERO;
        BigDecimal totalCommitted = BigDecimal.ZERO;

        List<BudgetVarianceItemDto> items = new ArrayList<>();

        for (Budget b : budgets) {
            BigDecimal baseline = b.getBaselineAmount() != null ? b.getBaselineAmount() : BigDecimal.ZERO;
            BigDecimal revised = (b.getRevisedAmount() != null && b.getRevisedAmount().compareTo(BigDecimal.ZERO) > 0)
                    ? b.getRevisedAmount()
                    : baseline;

            BigDecimal actual = transactions.stream()
                    .filter(t -> t.getCostCode() != null && t.getCostCode().equalsIgnoreCase(b.getCostCode()))
                    .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal committed = BigDecimal.ZERO;
            if (b.getCostHead() == CostHead.MATERIAL) {
                committed = pos.stream()
                        .filter(po -> po.getStatus() != null && List.of("ISSUED", "PARTIALLY_FULFILLED").contains(po.getStatus().toUpperCase()))
                        .map(po -> po.getAmount() != null ? po.getAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }

            BigDecimal projected = actual.add(committed);
            BigDecimal variance = revised.subtract(projected);

            String utilPct = "0.00%";
            if (revised.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal pct = projected.multiply(new BigDecimal("100")).divide(revised, 2, RoundingMode.HALF_UP);
                utilPct = pct + "%";
            }

            String status = "UNDER_BUDGET";
            if (revised.compareTo(BigDecimal.ZERO) > 0 && projected.compareTo(revised) > 0) {
                status = "OVER_BUDGET";
            } else if (revised.compareTo(BigDecimal.ZERO) > 0 && projected.compareTo(revised.multiply(new BigDecimal("0.90"))) >= 0) {
                status = "ON_BUDGET";
            }

            items.add(BudgetVarianceItemDto.builder()
                    .budgetId(b.getId())
                    .costCode(b.getCostCode())
                    .costHead(b.getCostHead())
                    .description(b.getDescription())
                    .baselineAmount(baseline)
                    .revisedAmount(revised)
                    .actualIncurred(actual)
                    .committedAmount(committed)
                    .totalProjected(projected)
                    .variance(variance)
                    .utilizationPercentage(utilPct)
                    .status(status)
                    .build());

            totalBaseline = totalBaseline.add(baseline);
            totalRevised = totalRevised.add(revised);
            totalActual = totalActual.add(actual);
            totalCommitted = totalCommitted.add(committed);
        }

        BigDecimal totalProjected = totalActual.add(totalCommitted);
        BigDecimal netVariance = totalRevised.subtract(totalProjected);

        String variancePct = "0.00%";
        if (totalRevised.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal pct = netVariance.multiply(new BigDecimal("100")).divide(totalRevised, 2, RoundingMode.HALF_UP);
            variancePct = pct + "%";
        }

        String overallStatus = "UNDER_BUDGET";
        if (totalRevised.compareTo(BigDecimal.ZERO) > 0 && totalProjected.compareTo(totalRevised.multiply(new BigDecimal("1.15"))) > 0) {
            overallStatus = "CRITICAL";
        } else if (totalRevised.compareTo(BigDecimal.ZERO) > 0 && totalProjected.compareTo(totalRevised) > 0) {
            overallStatus = "OVER_BUDGET";
        } else if (totalRevised.compareTo(BigDecimal.ZERO) > 0 && totalProjected.compareTo(totalRevised.multiply(new BigDecimal("0.90"))) >= 0) {
            overallStatus = "ON_BUDGET";
        }

        log.info("Generated Budget Variance Report for Project ID: {} ({}) | Revised: {}, Incurred: {}, Status: {}",
                projectId, projectName, totalRevised, totalActual, overallStatus);

        return BudgetVarianceReportDto.builder()
                .projectId(projectId)
                .projectName(projectName)
                .totalBaselineBudget(totalBaseline)
                .totalRevisedBudget(totalRevised)
                .totalActualExpense(totalActual)
                .totalCommittedExpense(totalCommitted)
                .totalProjectedCost(totalProjected)
                .netVariance(netVariance)
                .variancePercentage(variancePct)
                .overallStatus(overallStatus)
                .lineItems(items)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetOverrunReportDto getBudgetOverruns(Long projectId) {
        List<Budget> budgets = budgetRepository.findByProjectId(projectId);
        List<CostTransaction> transactions = costTransactionRepository.findByProjectId(projectId);

        List<BudgetOverrunReportDto.BudgetOverrunItemDto> overrunItems = new ArrayList<>();
        BigDecimal totalOverrun = BigDecimal.ZERO;

        for (Budget b : budgets) {
            BigDecimal allocated = (b.getRevisedAmount() != null && b.getRevisedAmount().compareTo(BigDecimal.ZERO) > 0)
                    ? b.getRevisedAmount()
                    : (b.getBaselineAmount() != null ? b.getBaselineAmount() : BigDecimal.ZERO);

            BigDecimal actual = transactions.stream()
                    .filter(t -> t.getCostCode() != null && t.getCostCode().equalsIgnoreCase(b.getCostCode()))
                    .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (allocated.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal ratio = actual.divide(allocated, 4, RoundingMode.HALF_UP);
                BigDecimal pct = ratio.multiply(new BigDecimal("100"));

                if (ratio.compareTo(BigDecimal.ONE) > 0) {
                    BigDecimal diff = actual.subtract(allocated);
                    totalOverrun = totalOverrun.add(diff);
                    overrunItems.add(BudgetOverrunReportDto.BudgetOverrunItemDto.builder()
                            .costCode(b.getCostCode())
                            .activityId(b.getActivityId())
                            .costHead(b.getCostHead() != null ? b.getCostHead().name() : "N/A")
                            .description(b.getDescription())
                            .allocatedBudget(allocated)
                            .actualExpense(actual)
                            .overrunAmount(diff)
                            .utilizationPercentage(pct.setScale(2, RoundingMode.HALF_UP) + "%")
                            .alertSeverity("CRITICAL_OVERRUN")
                            .alertMessage("CRITICAL: Budget limit exceeded by INR " + diff)
                            .build());
                } else if (ratio.compareTo(new BigDecimal("0.90")) >= 0) {
                    overrunItems.add(BudgetOverrunReportDto.BudgetOverrunItemDto.builder()
                            .costCode(b.getCostCode())
                            .activityId(b.getActivityId())
                            .costHead(b.getCostHead() != null ? b.getCostHead().name() : "N/A")
                            .description(b.getDescription())
                            .allocatedBudget(allocated)
                            .actualExpense(actual)
                            .overrunAmount(BigDecimal.ZERO)
                            .utilizationPercentage(pct.setScale(2, RoundingMode.HALF_UP) + "%")
                            .alertSeverity("WARNING_90_PERCENT")
                            .alertMessage("WARNING: Budget utilization reached " + pct.setScale(2, RoundingMode.HALF_UP) + "% (>= 90% threshold)")
                            .build());
                } else if (ratio.compareTo(new BigDecimal("0.80")) >= 0) {
                    overrunItems.add(BudgetOverrunReportDto.BudgetOverrunItemDto.builder()
                            .costCode(b.getCostCode())
                            .activityId(b.getActivityId())
                            .costHead(b.getCostHead() != null ? b.getCostHead().name() : "N/A")
                            .description(b.getDescription())
                            .allocatedBudget(allocated)
                            .actualExpense(actual)
                            .overrunAmount(BigDecimal.ZERO)
                            .utilizationPercentage(pct.setScale(2, RoundingMode.HALF_UP) + "%")
                            .alertSeverity("WARNING_80_PERCENT")
                            .alertMessage("WARNING: Budget utilization reached " + pct.setScale(2, RoundingMode.HALF_UP) + "% (>= 80% threshold)")
                            .build());
                }
            }
        }

        log.warn("Generated Budget Overrun Alerts for Project ID: {} | Overruns: {}, Overrun Amount: {}",
                projectId, overrunItems.size(), totalOverrun);

        return BudgetOverrunReportDto.builder()
                .projectId(projectId)
                .overrunCount(overrunItems.size())
                .totalOverrunAmount(totalOverrun)
                .overruns(overrunItems)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectCostTrackingReportDto getProjectCostTracking(Long projectId) {
        List<Budget> projectBudgets = budgetRepository.findByProjectId(projectId);
        List<CostTransaction> projectTxns = costTransactionRepository.findByProjectId(projectId);
        List<PurchaseOrder> projectPos = purchaseOrderRepository.findByProjectId(projectId);

        BigDecimal totalMaterialConsumption = stockLedgerRepository.getTotalMaterialConsumptionCostByProjectId(projectId);

        List<CostHeadTrackingDto> breakdown = new ArrayList<>();
        BigDecimal totalBaseline = BigDecimal.ZERO;
        BigDecimal totalApproved = BigDecimal.ZERO;
        BigDecimal totalActual = BigDecimal.ZERO;
        BigDecimal totalCommitted = BigDecimal.ZERO;

        for (CostHead head : CostHead.values()) {
            BigDecimal baseline = projectBudgets.stream()
                    .filter(b -> b.getCostHead() == head)
                    .map(b -> b.getBaselineAmount() != null ? b.getBaselineAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal approved = projectBudgets.stream()
                    .filter(b -> b.getCostHead() == head)
                    .map(b -> (b.getRevisedAmount() != null && b.getRevisedAmount().compareTo(BigDecimal.ZERO) > 0)
                            ? b.getRevisedAmount()
                            : (b.getBaselineAmount() != null ? b.getBaselineAmount() : BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal directCost = projectTxns.stream()
                    .filter(t -> t.getCostHead() == head)
                    .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal actualIncurred = directCost;
            if (head == CostHead.MATERIAL && totalMaterialConsumption != null) {
                actualIncurred = actualIncurred.add(totalMaterialConsumption);
            }

            BigDecimal committed = BigDecimal.ZERO;
            if (head == CostHead.MATERIAL) {
                committed = projectPos.stream()
                        .filter(po -> po.getStatus() != null && List.of("ISSUED", "PARTIALLY_FULFILLED").contains(po.getStatus().toUpperCase()))
                        .map(po -> po.getAmount() != null ? po.getAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }

            BigDecimal forecasted = actualIncurred.add(committed);
            BigDecimal variance = approved.subtract(forecasted);

            String utilPct = "0.00%";
            if (approved.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal pct = forecasted.multiply(new BigDecimal("100")).divide(approved, 2, RoundingMode.HALF_UP);
                utilPct = pct + "%";
            }

            String status = "UNDER_BUDGET";
            if (approved.compareTo(BigDecimal.ZERO) > 0 && forecasted.compareTo(approved) > 0) {
                status = "OVER_BUDGET";
            } else if (approved.compareTo(BigDecimal.ZERO) > 0 && forecasted.compareTo(approved.multiply(new BigDecimal("0.90"))) >= 0) {
                status = "ON_BUDGET";
            }

            breakdown.add(CostHeadTrackingDto.builder()
                    .costHead(head)
                    .baselineBudget(baseline)
                    .approvedBudget(approved)
                    .actualIncurred(actualIncurred)
                    .committedAmount(committed)
                    .totalForecasted(forecasted)
                    .variance(variance)
                    .utilizationPercentage(utilPct)
                    .status(status)
                    .build());

            totalBaseline = totalBaseline.add(baseline);
            totalApproved = totalApproved.add(approved);
            totalActual = totalActual.add(actualIncurred);
            totalCommitted = totalCommitted.add(committed);
        }

        BigDecimal totalProjected = totalActual.add(totalCommitted);
        BigDecimal netVariance = totalApproved.subtract(totalProjected);

        String overallStatus = "UNDER_BUDGET";
        if (totalApproved.compareTo(BigDecimal.ZERO) > 0 && totalProjected.compareTo(totalApproved) > 0) {
            overallStatus = "OVER_BUDGET";
        } else if (totalApproved.compareTo(BigDecimal.ZERO) > 0 && totalProjected.compareTo(totalApproved.multiply(new BigDecimal("0.90"))) >= 0) {
            overallStatus = "ON_BUDGET";
        }

        return ProjectCostTrackingReportDto.builder()
                .projectId(projectId)
                .totalBaselineBudget(totalBaseline)
                .totalApprovedBudget(totalApproved)
                .totalActualExpense(totalActual)
                .totalCommittedCost(totalCommitted)
                .totalProjectedCost(totalProjected)
                .netVariance(netVariance)
                .overallStatus(overallStatus)
                .costHeadBreakdown(breakdown)
                .build();
    }

    // ==========================================
    // Day 5: EVM Backend Support + Forecast
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public EvmMetricsDto calculateEvmMetrics(Long projectId) {
        return calculateEvmMetricsWithProgress(projectId, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public EvmMetricsDto calculateEvmMetricsWithProgress(Long projectId, Double plannedProgress, Double actualProgress) {
        BigDecimal bac = budgetRepository.getTotalApprovedBudgetByProjectId(projectId);
        BigDecimal pvBaseline = budgetRepository.getTotalBaselineBudgetByProjectId(projectId);
        if (bac == null) {
            bac = pvBaseline != null ? pvBaseline : BigDecimal.ZERO;
        }

        BigDecimal directCost = costTransactionRepository.getTotalActualCostByProjectId(projectId);
        BigDecimal materialConsumptionCost = stockLedgerRepository.getTotalMaterialConsumptionCostByProjectId(projectId);
        BigDecimal ac = directCost.add(materialConsumptionCost != null ? materialConsumptionCost : BigDecimal.ZERO);

        BigDecimal pv;
        if (plannedProgress != null) {
            pv = bac.multiply(BigDecimal.valueOf(plannedProgress)).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            pv = pvBaseline;
        }

        BigDecimal ev;
        if (actualProgress != null) {
            ev = bac.multiply(BigDecimal.valueOf(actualProgress)).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            ev = ac.compareTo(BigDecimal.ZERO) > 0 ? ac.min(bac) : BigDecimal.ZERO;
        }

        BigDecimal sv = ev.subtract(pv);
        BigDecimal cv = ev.subtract(ac);

        BigDecimal spi = pv.compareTo(BigDecimal.ZERO) > 0
                ? ev.divide(pv, 4, RoundingMode.HALF_UP)
                : BigDecimal.ONE;

        BigDecimal cpi = ac.compareTo(BigDecimal.ZERO) > 0
                ? ev.divide(ac, 4, RoundingMode.HALF_UP)
                : BigDecimal.ONE;

        BigDecimal eac = cpi.compareTo(BigDecimal.ZERO) > 0
                ? bac.divide(cpi, 2, RoundingMode.HALF_UP)
                : bac;

        BigDecimal etc = eac.subtract(ac).max(BigDecimal.ZERO);
        BigDecimal vac = bac.subtract(eac);

        BigDecimal bacMinusEv = bac.subtract(ev);
        BigDecimal bacMinusAc = bac.subtract(ac);
        BigDecimal tcpi = BigDecimal.ONE;
        if (bacMinusAc.compareTo(BigDecimal.ZERO) > 0) {
            tcpi = bacMinusEv.divide(bacMinusAc, 4, RoundingMode.HALF_UP);
        }

        String plannedPct = plannedProgress != null ? plannedProgress + "%" : "100.00%";
        String actualPct = actualProgress != null ? actualProgress + "%" : (bac.compareTo(BigDecimal.ZERO) > 0 ? ev.multiply(new BigDecimal("100")).divide(bac, 2, RoundingMode.HALF_UP) + "%" : "0.00%");

        String spiStatus = spi.compareTo(BigDecimal.ONE) >= 0 ? "ON_SCHEDULE" : "BEHIND_SCHEDULE";
        String cpiStatus = cpi.compareTo(BigDecimal.ONE) >= 0 ? "UNDER_BUDGET" : "OVER_BUDGET";

        return EvmMetricsDto.builder()
                .projectId(projectId)
                .budgetAtCompletion(bac)
                .plannedValue(pv)
                .earnedValue(ev)
                .actualCost(ac)
                .scheduleVariance(sv)
                .costVariance(cv)
                .schedulePerformanceIndex(spi)
                .costPerformanceIndex(cpi)
                .estimateAtCompletion(eac)
                .estimateToComplete(etc)
                .varianceAtCompletion(vac)
                .toCompletePerformanceIndex(tcpi)
                .plannedProgressPercentage(plannedPct)
                .actualProgressPercentage(actualPct)
                .spiStatus(spiStatus)
                .cpiStatus(cpiStatus)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EvmForecastDto getEvmForecast(Long projectId) {
        EvmMetricsDto evm = calculateEvmMetrics(projectId);

        String summary = String.format("EVM Forecast: BAC is INR %s, Current Actual Cost is INR %s. Estimated Cost at Completion (EAC) is INR %s with CPI of %s. Variance at Completion is INR %s.",
                evm.getBudgetAtCompletion(), evm.getActualCost(), evm.getEstimateAtCompletion(), evm.getCostPerformanceIndex(), evm.getVarianceAtCompletion());

        if (evm.getCostPerformanceIndex().compareTo(BigDecimal.ONE) < 0) {
            summary += " ALERT: Project is trending over budget; immediate cost control recommended.";
        } else {
            summary += " Project is trending on or under budget.";
        }

        log.debug("Computed EVM Forecast for Project ID: {} | EAC: {}, VAC: {}", projectId, evm.getEstimateAtCompletion(), evm.getVarianceAtCompletion());

        return EvmForecastDto.builder()
                .projectId(projectId)
                .budgetAtCompletion(evm.getBudgetAtCompletion())
                .plannedValue(evm.getPlannedValue())
                .earnedValue(evm.getEarnedValue())
                .actualCost(evm.getActualCost())
                .costPerformanceIndex(evm.getCostPerformanceIndex())
                .schedulePerformanceIndex(evm.getSchedulePerformanceIndex())
                .estimateAtCompletion(evm.getEstimateAtCompletion())
                .estimateToComplete(evm.getEstimateToComplete())
                .varianceAtCompletion(evm.getVarianceAtCompletion())
                .forecastSummary(summary)
                .build();
    }

    @Override
    @org.springframework.scheduling.annotation.Scheduled(cron = "${app.cost.overrun-check.cron:0 0 1 * * ?}")
    public void runDailyBudgetOverrunCheck() {
        log.info("Running daily background automated budget variance & overrun scan: Allocated Budget - (Actual Cost + Committed Cost)");
        List<com.example.BuildTwin._0.domain.projects.model.Project> projects = projectRepository.findAll();
        for (var project : projects) {
            try {
                BudgetOverrunReportDto report = getBudgetOverruns(project.getId());
                if (report.getOverrunCount() > 0) {
                    log.warn("[BUDGET OVERRUN ALERT] Project ID: {} ({}) has {} WBS activities crossing 80%/90% or exceeding budget! Overrun Amount: INR {}",
                            project.getId(), project.getName(), report.getOverrunCount(), report.getTotalOverrunAmount());
                    for (var item : report.getOverruns()) {
                        log.warn(" --> WBS Alert: CostCode: {}, Head: {}, Utilization: {}, Severity: {}, Message: {}",
                                item.getCostCode(), item.getCostHead(), item.getUtilizationPercentage(), item.getAlertSeverity(), item.getAlertMessage());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to run budget overrun check for project {}: {}", project.getId(), e.getMessage());
            }
        }
    }

    @Override
    public CostSyncResultDto syncMaterialCost(Long projectId) {
        log.info("Syncing material consumption into Cost Actual Ledger for Project ID: {}", projectId);
        List<StockLedger> consumptionEntries = stockLedgerRepository.findByProjectId(projectId).stream()
                .filter(e -> com.example.BuildTwin._0.domain.materials.enums.StockTransactionType.CONSUMPTION.equals(e.getTransactionType()))
                .toList();

        int matCount = 0;
        BigDecimal totalSynced = BigDecimal.ZERO;

        for (StockLedger entry : consumptionEntries) {
            boolean exists = costActualLedgerRepository.existsBySourceTypeAndSourceReferenceId("STOCK_CONSUMPTION", entry.getId());
            if (!exists) {
                BigDecimal qty = entry.getQuantity() != null ? entry.getQuantity() : BigDecimal.ZERO;
                BigDecimal price = entry.getUnitPrice() != null ? entry.getUnitPrice() : BigDecimal.ZERO;
                BigDecimal cost = qty.multiply(price).setScale(2, RoundingMode.HALF_UP);

                if (cost.compareTo(BigDecimal.ZERO) > 0) {
                    costActualLedgerRepository.save(CostActualLedger.builder()
                            .projectId(projectId)
                            .wbsActivityId(entry.getActivityId())
                            .sourceType("STOCK_CONSUMPTION")
                            .sourceReferenceId(entry.getId())
                            .costCode("MATERIAL")
                            .incurredQty(qty)
                            .unitRate(price)
                            .totalCost(cost)
                            .expenseDate(entry.getTimestamp() != null ? entry.getTimestamp().toLocalDate() : LocalDate.now())
                            .build());

                    costTransactionRepository.save(CostTransaction.builder()
                            .projectId(projectId)
                            .activityId(entry.getActivityId())
                            .costCode("MATERIAL_CONSUMPTION")
                            .costHead(CostHead.MATERIAL)
                            .sourceType(CostSourceType.ACTUAL_EXPENSE)
                            .amount(cost)
                            .transactionDate(entry.getTimestamp() != null ? entry.getTimestamp().toLocalDate() : LocalDate.now())
                            .referenceNumber("STK-CONS-" + entry.getId())
                            .remarks("Auto-synced from Stock Ledger consumption #" + entry.getId())
                            .build());

                    matCount++;
                    totalSynced = totalSynced.add(cost);
                }
            }
        }

        totalSynced = totalSynced.setScale(2, RoundingMode.HALF_UP);
        return CostSyncResultDto.builder()
                .projectId(projectId)
                .materialTransactionsSynced(matCount)
                .labourTransactionsSynced(0)
                .totalSyncedAmount(totalSynced)
                .message("Material consumption successfully synchronized into Cost Actual Ledger")
                .build();
    }

    @Override
    public CostSyncResultDto syncLabourCost(Long projectId) {
        log.info("Syncing labour attendance into Cost Actual Ledger for Project ID: {}", projectId);
        List<LabourDaily> labourRecords = labourDailyRepository.findByProjectId(projectId);

        int labCount = 0;
        BigDecimal totalSynced = BigDecimal.ZERO;

        for (LabourDaily rec : labourRecords) {
            boolean exists = costActualLedgerRepository.existsBySourceTypeAndSourceReferenceId("LABOUR_TIMESHEET", rec.getId());
            if (!exists) {
                int hc = rec.getHeadcount() != null ? rec.getHeadcount() : 1;
                BigDecimal stdHrs = rec.getStandardHours() != null ? rec.getStandardHours() : new BigDecimal("8.00");
                BigDecimal otHrs = rec.getOvertimeHours() != null ? rec.getOvertimeHours() : BigDecimal.ZERO;
                BigDecimal rate = getTradeRate(rec.getTradeCategory());
                BigDecimal stdCost = stdHrs.multiply(BigDecimal.valueOf(hc)).multiply(rate);
                BigDecimal otCost = otHrs.multiply(BigDecimal.valueOf(hc)).multiply(rate).multiply(new BigDecimal("1.50"));
                BigDecimal cost = stdCost.add(otCost).setScale(2, RoundingMode.HALF_UP);

                Long wbsId = (rec.getAllocations() != null && !rec.getAllocations().isEmpty())
                        ? rec.getAllocations().get(0).getWbsActivityId()
                        : null;

                if (cost.compareTo(BigDecimal.ZERO) > 0) {
                    costActualLedgerRepository.save(CostActualLedger.builder()
                            .projectId(projectId)
                            .wbsActivityId(wbsId)
                            .sourceType("LABOUR_TIMESHEET")
                            .sourceReferenceId(rec.getId())
                            .costCode("LABOUR")
                            .incurredQty(stdHrs.add(otHrs).multiply(BigDecimal.valueOf(hc)))
                            .unitRate(rate)
                            .totalCost(cost)
                            .expenseDate(rec.getRecordDate() != null ? rec.getRecordDate() : LocalDate.now())
                            .build());

                    costTransactionRepository.save(CostTransaction.builder()
                            .projectId(projectId)
                            .costCode("LABOUR_WAGES")
                            .costHead(CostHead.LABOUR)
                            .sourceType(CostSourceType.LABOUR_WAGES)
                            .amount(cost)
                            .transactionDate(rec.getRecordDate() != null ? rec.getRecordDate() : LocalDate.now())
                            .referenceNumber("LAB-REC-" + rec.getId())
                            .vendorOrPayee(rec.getContractor() != null ? rec.getContractor().getName() : "Direct Labour")
                            .remarks("Auto-synced from Daily Labour Record #" + rec.getId())
                            .build());

                    labCount++;
                    totalSynced = totalSynced.add(cost);
                }
            }
        }

        totalSynced = totalSynced.setScale(2, RoundingMode.HALF_UP);
        return CostSyncResultDto.builder()
                .projectId(projectId)
                .materialTransactionsSynced(0)
                .labourTransactionsSynced(labCount)
                .equipmentTransactionsSynced(0)
                .totalSyncedAmount(totalSynced)
                .message("Contractor labour timesheet attendance successfully synchronized into Cost Actual Ledger")
                .build();
    }

    @Override
    public CostSyncResultDto syncEquipmentCost(Long projectId) {
        log.info("Syncing equipment usage logsheets into Cost Actual Ledger for Project ID: {}", projectId);
        List<com.example.BuildTwin._0.domain.equipment.model.EquipmentUsage> usageRecords = equipmentUsageRepository.findByProjectId(projectId);

        int eqCount = 0;
        BigDecimal totalSynced = BigDecimal.ZERO;
        BigDecimal standardHourlyRate = defaultEquipmentHourlyRate; // Configurable equipment hourly operational rate

        for (com.example.BuildTwin._0.domain.equipment.model.EquipmentUsage rec : usageRecords) {
            boolean exists = costActualLedgerRepository.existsBySourceTypeAndSourceReferenceId("EQUIPMENT_USAGE", rec.getId());
            if (!exists) {
                BigDecimal hrs = rec.getHoursUsed() != null ? rec.getHoursUsed() : BigDecimal.ZERO;
                BigDecimal cost = hrs.multiply(standardHourlyRate).setScale(2, RoundingMode.HALF_UP);

                if (cost.compareTo(BigDecimal.ZERO) > 0) {
                    costActualLedgerRepository.save(CostActualLedger.builder()
                            .projectId(projectId)
                            .wbsActivityId(rec.getActivityId())
                            .sourceType("EQUIPMENT_USAGE")
                            .sourceReferenceId(rec.getId())
                            .costCode("EQUIPMENT")
                            .incurredQty(hrs)
                            .unitRate(standardHourlyRate)
                            .totalCost(cost)
                            .expenseDate(rec.getUsageDate() != null ? rec.getUsageDate() : LocalDate.now())
                            .build());

                    costTransactionRepository.save(CostTransaction.builder()
                            .projectId(projectId)
                            .activityId(rec.getActivityId())
                            .costCode("EQUIPMENT_USAGE")
                            .costHead(CostHead.EQUIPMENT)
                            .sourceType(CostSourceType.ACTUAL_EXPENSE)
                            .amount(cost)
                            .transactionDate(rec.getUsageDate() != null ? rec.getUsageDate() : LocalDate.now())
                            .referenceNumber("EQ-USE-" + rec.getId())
                            .remarks("Auto-synced from Equipment usage #" + rec.getId())
                            .build());

                    eqCount++;
                    totalSynced = totalSynced.add(cost);
                }
            }
        }

        totalSynced = totalSynced.setScale(2, RoundingMode.HALF_UP);
        return CostSyncResultDto.builder()
                .projectId(projectId)
                .materialTransactionsSynced(0)
                .labourTransactionsSynced(0)
                .equipmentTransactionsSynced(eqCount)
                .totalSyncedAmount(totalSynced)
                .message("Equipment fleet usage hours successfully synchronized into Cost Actual Ledger")
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public ProjectCostTrackingReportDto getActualSpendBreakdown(Long projectId) {
        return getProjectCostTracking(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProcurementExpenditureTrailDto getProcurementLiabilityGap(Long projectId) {
        return getProcurementExpenditureTrail(projectId);
    }

    @Override
    public ProjectEVMRecord calculateAndSaveEvmSnapshot(Long projectId, Double plannedProgress, Double actualProgress) {
        EvmMetricsDto metrics = calculateEvmMetricsWithProgress(projectId, plannedProgress, actualProgress);
        LocalDate today = LocalDate.now();

        ProjectEVMRecord record = projectEVMRecordRepository.findByProjectIdAndEvaluationDate(projectId, today)
                .orElse(ProjectEVMRecord.builder()
                        .projectId(projectId)
                        .evaluationDate(today)
                        .build());

        record.setPlannedValue(metrics.getPlannedValue());
        record.setEarnedValue(metrics.getEarnedValue());
        record.setActualCost(metrics.getActualCost());
        record.setBudgetAtCompletion(metrics.getBudgetAtCompletion());
        record.setCostVariance(metrics.getCostVariance());
        record.setScheduleVariance(metrics.getScheduleVariance());
        record.setCpi(metrics.getCostPerformanceIndex());
        record.setSpi(metrics.getSchedulePerformanceIndex());
        record.setEstimateAtCompletion(metrics.getEstimateAtCompletion());
        record.setCostStatus(metrics.getCpiStatus());
        record.setScheduleStatus(metrics.getSpiStatus());

        ProjectEVMRecord saved = projectEVMRecordRepository.save(record);
        log.info("Saved EVM historical record for Project: {} | Date: {} | CPI: {}, SPI: {}, EAC: {}",
                projectId, today, saved.getCpi(), saved.getSpi(), saved.getEstimateAtCompletion());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public EvmMetricsDto getEvmDashboard(Long projectId) {
        return calculateEvmMetrics(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectEVMRecord> getEvmTrends(Long projectId) {
        return projectEVMRecordRepository.findByProjectIdOrderByEvaluationDateAsc(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectEVMRecord> getEvmTrends(Long projectId, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return projectEVMRecordRepository.findByProjectIdAndEvaluationDateBetweenOrderByEvaluationDateAsc(projectId, startDate, endDate);
        }
        return projectEVMRecordRepository.findByProjectIdOrderByEvaluationDateAsc(projectId);
    }

    @Override
    public CostTransaction updateCostTransaction(Long id, CostTransactionRequestDto dto) {
        CostTransaction existing = getCostTransactionById(id);
        if (dto.getProjectId() != null) existing.setProjectId(dto.getProjectId());
        if (dto.getActivityId() != null) existing.setActivityId(dto.getActivityId());
        if (dto.getCostCode() != null) existing.setCostCode(dto.getCostCode());
        if (dto.getCostHead() != null) existing.setCostHead(dto.getCostHead());
        if (dto.getSourceType() != null) existing.setSourceType(dto.getSourceType());
        if (dto.getAmount() != null) existing.setAmount(dto.getAmount());
        if (dto.getTransactionDate() != null) existing.setTransactionDate(dto.getTransactionDate());
        if (dto.getReferenceNumber() != null) existing.setReferenceNumber(dto.getReferenceNumber());
        if (dto.getVendorOrPayee() != null) existing.setVendorOrPayee(dto.getVendorOrPayee());
        if (dto.getRemarks() != null) existing.setRemarks(dto.getRemarks());

        CostTransaction updated = costTransactionRepository.save(existing);
        log.info("Updated Cost Transaction ID: {} | New Amount: {}, Ref: '{}'", id, updated.getAmount(), updated.getReferenceNumber());
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CostActualLedger> getCostActualLedgersByProject(Long projectId) {
        return costActualLedgerRepository.findByProjectId(projectId);
    }

    @Override
    public void deleteCostActualLedger(Long id) {
        CostActualLedger ledger = costActualLedgerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CostActualLedger not found with ID: " + id));
        costActualLedgerRepository.delete(ledger);
        log.warn("Deleted Cost Actual Ledger entry ID: {} for Project: {}", id, ledger.getProjectId());
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectEVMRecord getEvmRecordById(Long id) {
        return projectEVMRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectEVMRecord not found with ID: " + id));
    }

    @Override
    public void deleteEvmRecord(Long id) {
        ProjectEVMRecord rec = getEvmRecordById(id);
        projectEVMRecordRepository.delete(rec);
        log.warn("Deleted ProjectEVMRecord snapshot ID: {} for Project: {}", id, rec.getProjectId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectBudget> getProjectBudgetsByProject(Long projectId) {
        return projectBudgetRepository.findByProjectId(projectId);
    }
}
