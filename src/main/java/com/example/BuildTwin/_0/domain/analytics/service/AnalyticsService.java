package com.example.BuildTwin._0.domain.analytics.service;

import com.example.BuildTwin._0.domain.analytics.dto.DelayRiskDto;
import com.example.BuildTwin._0.domain.analytics.dto.ProjectHealthDto;

public interface AnalyticsService {
    ProjectHealthDto calculateProjectHealth(Long projectId);
    DelayRiskDto calculateDelayRisk(Long projectId);
}
