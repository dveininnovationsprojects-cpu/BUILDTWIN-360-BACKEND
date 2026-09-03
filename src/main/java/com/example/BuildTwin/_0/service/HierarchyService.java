package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.hierarchy.HierarchyTreeResponse;
import com.example.BuildTwin._0.dto.hierarchy.HierarchyValidationResponse;

public interface HierarchyService {

    HierarchyValidationResponse validateHierarchy(
            Long projectId, Long siteId, Long buildingId, Long floorId, Long zoneId);

    HierarchyTreeResponse getProjectHierarchyTree(Long projectId);
}
