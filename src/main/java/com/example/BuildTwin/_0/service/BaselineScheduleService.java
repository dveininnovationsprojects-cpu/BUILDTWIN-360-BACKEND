package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.baseline.*;

import java.util.List;

public interface BaselineScheduleService {

    ProjectBaselineResponse createBaseline(Long projectId, CreateProjectBaselineRequest request, String performedBy);

    List<ProjectBaselineResponse> getBaselinesByProject(Long projectId);

    ProjectBaselineResponse getBaselineById(Long baselineId);

    List<BaselineActivitySnapshotResponse> getBaselineSnapshots(Long baselineId);

    ProjectBaselineResponse setActiveBaseline(Long projectId, Long baselineId, String performedBy);

    ProjectBaselineResponse approveBaseline(Long baselineId, String approvedBy);

    void deleteBaseline(Long baselineId, String performedBy);

    ProjectScheduleVarianceReport getProjectScheduleVarianceReport(Long projectId, Long optionalBaselineId);

    ActivityScheduleVarianceResponse getActivityScheduleVariance(Long activityId, Long optionalBaselineId);

    ActivityProgressLogResponse logDailyProgress(Long activityId, LogDailyProgressRequest request, String recordedBy);

    List<ActivityProgressLogResponse> getActivityProgressLogs(Long activityId);
}
