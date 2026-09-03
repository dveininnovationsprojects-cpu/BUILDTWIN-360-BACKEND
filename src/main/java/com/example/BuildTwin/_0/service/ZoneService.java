package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.zone.CreateZoneRequest;
import com.example.BuildTwin._0.dto.zone.UpdateZoneRequest;
import com.example.BuildTwin._0.dto.zone.UpdateZoneStatusRequest;
import com.example.BuildTwin._0.dto.zone.ZoneResponse;

import java.util.List;

public interface ZoneService {

    ZoneResponse createZone(Long floorId, CreateZoneRequest request, String performedBy);

    List<ZoneResponse> getZonesByFloorId(Long floorId);

    PageResponse<ZoneResponse> getZonesByFloorIdPaginated(Long floorId, int page, int size);

    ZoneResponse getZoneById(Long id);

    ZoneResponse updateZone(Long id, UpdateZoneRequest request, String performedBy);

    ZoneResponse updateZoneStatus(Long id, UpdateZoneStatusRequest request, String performedBy);

    void deleteZone(Long id, String performedBy);
}
