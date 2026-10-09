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

    List<Budget> getBudgetsByProject(Long projectId);

    void deleteBudget(Long id);

    CostTransaction recordCostTransaction(CostTransactionRequestDto dto);

    CostTransaction getCostTransactionById(Long id);

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
}
