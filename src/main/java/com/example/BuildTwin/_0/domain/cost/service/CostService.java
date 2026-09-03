package com.example.BuildTwin._0.domain.cost.service;

import com.example.BuildTwin._0.domain.cost.dto.BudgetRequestDto;
import com.example.BuildTwin._0.domain.cost.dto.CostTransactionRequestDto;
import com.example.BuildTwin._0.domain.cost.dto.EvmMetricsDto;
import com.example.BuildTwin._0.domain.cost.model.Budget;
import com.example.BuildTwin._0.domain.cost.model.CostTransaction;

import java.util.List;

public interface CostService {

    Budget createOrUpdateBudget(BudgetRequestDto dto);

    List<Budget> getBudgetsByProject(Long projectId);

    CostTransaction recordCostTransaction(CostTransactionRequestDto dto);

    List<CostTransaction> getCostTransactionsByProject(Long projectId);

    EvmMetricsDto calculateEvmMetrics(Long projectId);
}
