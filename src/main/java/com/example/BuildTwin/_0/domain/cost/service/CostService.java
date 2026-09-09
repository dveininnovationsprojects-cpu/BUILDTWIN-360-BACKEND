package com.example.BuildTwin._0.domain.cost.service;

import com.example.BuildTwin._0.domain.cost.dto.BudgetRequestDto;
import com.example.BuildTwin._0.domain.cost.dto.CostTransactionRequestDto;
import com.example.BuildTwin._0.domain.cost.dto.EvmMetricsDto;
import com.example.BuildTwin._0.domain.cost.model.Budget;
import com.example.BuildTwin._0.domain.cost.model.CostTransaction;

import java.util.List;

public interface CostService {

    Budget createOrUpdateBudget(BudgetRequestDto dto);

    Budget getBudgetById(Long id);

    List<Budget> getBudgetsByProject(Long projectId);

    void deleteBudget(Long id);

    CostTransaction recordCostTransaction(CostTransactionRequestDto dto);

    CostTransaction getCostTransactionById(Long id);

    List<CostTransaction> getCostTransactionsByProject(Long projectId);

    void deleteCostTransaction(Long id);

    EvmMetricsDto calculateEvmMetrics(Long projectId);
}
