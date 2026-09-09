package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.site.CreateSiteRequest;
import com.example.BuildTwin._0.dto.site.SiteResponse;
import com.example.BuildTwin._0.dto.site.UpdateSiteRequest;
import com.example.BuildTwin._0.dto.site.UpdateSiteStatusRequest;

import java.util.List;

public interface SiteService {

    SiteResponse createSite(Long projectId, CreateSiteRequest request, String performedBy);

    List<SiteResponse> getSitesByProjectId(Long projectId);

    PageResponse<SiteResponse> getSitesByProjectIdPaginated(Long projectId, int page, int size);

    SiteResponse getSiteById(Long id);

    SiteResponse updateSite(Long id, UpdateSiteRequest request, String performedBy);

    SiteResponse updateSiteStatus(Long id, UpdateSiteStatusRequest request, String performedBy);

    void deleteSite(Long id, String performedBy);
}
