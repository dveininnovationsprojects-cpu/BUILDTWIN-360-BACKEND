package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.wbs.*;

import java.util.List;

public interface WbsActivityService {

    WbsActivityResponse createActivity(Long workPackageId, CreateWbsActivityRequest request, String performedBy);

    WbsActivityResponse createChildActivity(Long parentActivityId, CreateWbsActivityRequest request, String performedBy);

    WbsActivityResponse reparentActivity(Long id, ReparentWbsActivityRequest request, String performedBy);

    List<WbsActivityResponse> getChildActivities(Long parentId);

    WbsActivityResponse getActivityTree(Long activityId);

    List<WbsActivityResponse> getActivitiesByWorkPackageId(Long workPackageId);

    PageResponse<WbsActivityResponse> getActivitiesByWorkPackageIdPaginated(
            Long workPackageId, String status, int page, int size);

    PageResponse<WbsActivityResponse> getActivitiesByProjectId(
            Long projectId, String status, String discipline, int page, int size, String sortBy, String sortDir);

    PageResponse<WbsActivityResponse> searchActivities(
            Long projectId, Long workPackageId, Long siteId, Long buildingId, Long floorId, Long zoneId,
            String discipline, String status, String contractor, Long inchargeUserId,
            int page, int size, String sortBy, String sortDir);

    WbsActivityResponse getActivityById(Long id);

    WbsActivityResponse updateActivity(Long id, UpdateWbsActivityRequest request, String performedBy);

    WbsActivityResponse updateActivityProgress(Long id, UpdateWbsActivityProgressRequest request, String performedBy);

    WbsActivityResponse updateActivityStatus(Long id, UpdateWbsActivityStatusRequest request, String performedBy);

    WbsActivityResponse assignActivity(Long id, AssignActivityRequest request, String performedBy);

    WbsActivityResponse scheduleActivity(Long id, ScheduleActivityRequest request, String performedBy);

    WbsActivityResponse relocateActivity(Long id, RelocateActivityRequest request, String performedBy);

    void deleteActivity(Long id, String performedBy);

    WbsSummaryResponse getWbsSummary(Long projectId);

    WbsTreeResponse getWbsTree(Long projectId);

    WbsTreeResponse.WorkPackageNode getWorkPackageWbsTree(Long workPackageId);
}
