package com.example.BuildTwin._0.domain.reports.service;

import com.example.BuildTwin._0.domain.reports.dto.*;

public interface ReportService {

    /**
     * Week 10 Day 1: Weekly Project Report API
     */
    WeeklyProjectReportDto generateWeeklyProjectReport(Long projectId, Integer weekNumber);

    /**
     * Week 10 Day 2: Material & Inventory Reports
     */
    InventoryReportDto generateInventoryReport(Long projectId);

    /**
     * Week 10 Day 3: Procurement & Labour Reports
     */
    ProcurementLabourReportDto generateProcurementLabourReport(Long projectId);

    /**
     * Week 10 Day 4: Cost & Quality Reports
     */
    CostQualityReportDto generateCostQualityReport(Long projectId);

    /**
     * Week 10 Day 5: Analytics Aggregation APIs
     */
    AnalyticsAggregationDto generateAnalyticsAggregation(Long projectId);
}
