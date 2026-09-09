package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.floor.CreateFloorRequest;
import com.example.BuildTwin._0.dto.floor.FloorResponse;
import com.example.BuildTwin._0.dto.floor.UpdateFloorRequest;
import com.example.BuildTwin._0.dto.floor.UpdateFloorStatusRequest;

import java.util.List;

public interface FloorService {

    FloorResponse createFloor(Long buildingId, CreateFloorRequest request, String performedBy);

    List<FloorResponse> getFloorsByBuildingId(Long buildingId);

    PageResponse<FloorResponse> getFloorsByBuildingIdPaginated(Long buildingId, int page, int size);

    FloorResponse getFloorById(Long id);

    FloorResponse updateFloor(Long id, UpdateFloorRequest request, String performedBy);

    FloorResponse updateFloorStatus(Long id, UpdateFloorStatusRequest request, String performedBy);

    void deleteFloor(Long id, String performedBy);
}
