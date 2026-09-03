package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.wbs.CreateWorkPackageRequest;
import com.example.BuildTwin._0.dto.wbs.UpdateWorkPackageRequest;
import com.example.BuildTwin._0.dto.wbs.UpdateWorkPackageStatusRequest;
import com.example.BuildTwin._0.dto.wbs.WorkPackageResponse;

public interface WorkPackageService {

    WorkPackageResponse createWorkPackage(Long projectId, CreateWorkPackageRequest request, String performedBy);

    PageResponse<WorkPackageResponse> getWorkPackagesByProjectId(
            Long projectId, String status, String discipline, int page, int size, String sortBy, String sortDir);

    WorkPackageResponse getWorkPackageById(Long id);

    WorkPackageResponse updateWorkPackage(Long id, UpdateWorkPackageRequest request, String performedBy);

    WorkPackageResponse updateWorkPackageStatus(Long id, UpdateWorkPackageStatusRequest request, String performedBy);

    void deleteWorkPackage(Long id, String performedBy);
}
