package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.building.BuildingResponse;
import com.example.BuildTwin._0.dto.building.CreateBuildingRequest;
import com.example.BuildTwin._0.dto.building.UpdateBuildingRequest;
import com.example.BuildTwin._0.dto.building.UpdateBuildingStatusRequest;
import com.example.BuildTwin._0.dto.common.PageResponse;

import java.util.List;

public interface BuildingService {

    BuildingResponse createBuilding(Long siteId, CreateBuildingRequest request, String performedBy);

    List<BuildingResponse> getBuildingsBySiteId(Long siteId);

    PageResponse<BuildingResponse> getBuildingsBySiteIdPaginated(Long siteId, int page, int size);

    BuildingResponse getBuildingById(Long id);

    BuildingResponse updateBuilding(Long id, UpdateBuildingRequest request, String performedBy);

    BuildingResponse updateBuildingStatus(Long id, UpdateBuildingStatusRequest request, String performedBy);

    void deleteBuilding(Long id, String performedBy);
}
