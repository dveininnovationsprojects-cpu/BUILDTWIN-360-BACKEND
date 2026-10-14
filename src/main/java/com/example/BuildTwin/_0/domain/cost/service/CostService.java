package com.example.BuildTwin._0.domain.cost.service;

import com.example.BuildTwin._0.domain.cost.dto.BudgetRequestDto;
import com.example.BuildTwin._0.domain.cost.dto.CostTransactionRequestDto;
import com.example.BuildTwin._0.domain.cost.dto.EvmMetricsDto;
import com.example.BuildTwin._0.domain.cost.model.Budget;
import com.example.BuildTwin._0.domain.cost.model.CostTransaction;

import java.util.List;

public interface CostService {

    Budget createOrUpdateBudget(BudgetRequestDto dto);

    Budget updateBudget(Long id, BudgetRequestDto dto);

    Budget authorizeBudgetRevision(Long id, com.example.BuildTwin._0.domain.cost.dto.BudgetRevisionDto dto);

    Budget getBudgetById(Long id);

    List<Budget> getAllBudgets();

    List<Budget> getBudgetsByProject(Long projectId);

    List<Budget> getBudgetsByProjectAndActivity(Long projectId, Long activityId);

    void deleteBudget(Long id);

    CostTransaction recordCostTransaction(CostTransactionRequestDto dto);

    CostTransaction getCostTransactionById(Long id);

    List<CostTransaction> getAllCostTransactions();

    List<CostTransaction> getCostTransactionsByProject(Long projectId);

    void deleteCostTransaction(Long id);

    com.example.BuildTwin._0.domain.cost.dto.ProjectCostTrackingReportDto getProjectCostTracking(Long projectId);

    com.example.BuildTwin._0.domain.cost.dto.ProcurementCommitmentDto getProcurementCommitments(Long projectId);

    com.example.BuildTwin._0.domain.cost.dto.BudgetVarianceReportDto getBudgetVarianceReport(Long projectId);

    EvmMetricsDto calculateEvmMetrics(Long projectId);

    EvmMetricsDto calculateEvmMetricsWithProgress(Long projectId, Double plannedProgress, Double actualProgress);

    com.example.BuildTwin._0.domain.cost.dto.MaterialActualCostSummaryDto getMaterialActualCostSummary(Long projectId);

    com.example.BuildTwin._0.domain.cost.dto.LabourActualCostSummaryDto getLabourActualCostSummary(Long projectId);

    com.example.BuildTwin._0.domain.cost.dto.CostSyncResultDto syncActualCosts(Long projectId);

    com.example.BuildTwin._0.domain.cost.dto.ProcurementExpenditureTrailDto getProcurementExpenditureTrail(Long projectId);

    com.example.BuildTwin._0.domain.cost.dto.BudgetOverrunReportDto getBudgetOverruns(Long projectId);

    com.example.BuildTwin._0.domain.cost.dto.EvmForecastDto getEvmForecast(Long projectId);

    void runDailyBudgetOverrunCheck();

    com.example.BuildTwin._0.domain.cost.dto.CostSyncResultDto syncMaterialCost(Long projectId);

    com.example.BuildTwin._0.domain.cost.dto.CostSyncResultDto syncLabourCost(Long projectId);

    com.example.BuildTwin._0.domain.cost.dto.CostSyncResultDto syncEquipmentCost(Long projectId);

    com.example.BuildTwin._0.domain.cost.dto.ProjectCostTrackingReportDto getActualSpendBreakdown(Long projectId);

    com.example.BuildTwin._0.domain.cost.dto.ProcurementExpenditureTrailDto getProcurementLiabilityGap(Long projectId);

    com.example.BuildTwin._0.domain.cost.model.ProjectEVMRecord calculateAndSaveEvmSnapshot(Long projectId, Double plannedProgress, Double actualProgress);

    EvmMetricsDto getEvmDashboard(Long projectId);

    java.util.List<com.example.BuildTwin._0.domain.cost.model.ProjectEVMRecord> getEvmTrends(Long projectId);

    java.util.List<com.example.BuildTwin._0.domain.cost.model.ProjectEVMRecord> getEvmTrends(Long projectId, java.time.LocalDate startDate, java.time.LocalDate endDate);

    CostTransaction updateCostTransaction(Long id, CostTransactionRequestDto dto);

    java.util.List<com.example.BuildTwin._0.domain.cost.model.CostActualLedger> getCostActualLedgersByProject(Long projectId);

    void deleteCostActualLedger(Long id);

    com.example.BuildTwin._0.domain.cost.model.ProjectEVMRecord getEvmRecordById(Long id);

    void deleteEvmRecord(Long id);

    java.util.List<com.example.BuildTwin._0.domain.cost.model.ProjectBudget> getProjectBudgetsByProject(Long projectId);
}
