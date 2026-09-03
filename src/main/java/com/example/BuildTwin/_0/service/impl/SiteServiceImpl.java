package com.example.BuildTwin._0.service.impl;

import com.example.BuildTwin._0.dto.common.PageResponse;
import com.example.BuildTwin._0.dto.site.CreateSiteRequest;
import com.example.BuildTwin._0.dto.site.SiteResponse;
import com.example.BuildTwin._0.dto.site.UpdateSiteRequest;
import com.example.BuildTwin._0.dto.site.UpdateSiteStatusRequest;
import com.example.BuildTwin._0.exception.BadRequestException;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import com.example.BuildTwin._0.model.Project;
import com.example.BuildTwin._0.model.Site;
import com.example.BuildTwin._0.repository.ProjectRepository;
import com.example.BuildTwin._0.repository.SiteRepository;
import com.example.BuildTwin._0.service.AuditService;
import com.example.BuildTwin._0.service.SiteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService {

    private static final Set<String> ALLOWED_SITE_STATUSES = Set.of(
            "PLANNED", "ACTIVE", "INACTIVE", "COMPLETED"
    );

    private static final Set<String> ALLOWED_SITE_TYPES = Set.of(
            "BUILDING_TOWER", "INFRASTRUCTURE", "CLUBHOUSE", "AMENITIES", "VILLA_ZONE", "UTILITIES", "EXTERNAL_WORKS"
    );

    private final SiteRepository siteRepository;
    private final ProjectRepository projectRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public SiteResponse createSite(Long projectId, CreateSiteRequest request, String performedBy) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        String code = request.getCode().trim().toUpperCase();
        if (siteRepository.existsByProjectIdAndCode(projectId, code)) {
            throw new DuplicateResourceException("Site", "code", code + " under Project " + project.getCode());
        }

        String siteStatus = request.getStatus() != null ? validateAndNormalizeSiteStatus(request.getStatus()) : "ACTIVE";
        String siteType = request.getSiteType() != null ? validateAndNormalizeSiteType(request.getSiteType()) : "BUILDING_TOWER";

        Site site = Site.builder()
                .project(project)
                .code(code)
                .name(request.getName().trim())
                .siteType(siteType)
                .location(request.getLocation())
                .status(siteStatus)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .areaSqFt(request.getAreaSqFt())
                .siteIncharge(request.getSiteIncharge())
                .build();

        Site savedSite = siteRepository.save(site);

        auditService.logAction(
                performedBy,
                "CREATE_SITE",
                "SITE",
                String.valueOf(savedSite.getId()),
                "Created site '" + savedSite.getName() + "' (" + savedSite.getCode() + ") under project: " + project.getName(),
                null
        );

        return mapToSiteResponse(savedSite);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SiteResponse> getSitesByProjectId(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        return siteRepository.findByProjectId(projectId).stream()
                .map(this::mapToSiteResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SiteResponse> getSitesByProjectIdPaginated(Long projectId, int page, int size) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Site> sitePage = siteRepository.findByProjectId(projectId, pageable);

        List<SiteResponse> content = sitePage.getContent().stream()
                .map(this::mapToSiteResponse)
                .collect(Collectors.toList());

        return PageResponse.<SiteResponse>builder()
                .content(content)
                .pageNumber(sitePage.getNumber())
                .pageSize(sitePage.getSize())
                .totalElements(sitePage.getTotalElements())
                .totalPages(sitePage.getTotalPages())
                .isFirst(sitePage.isFirst())
                .isLast(sitePage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SiteResponse getSiteById(Long id) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site", "id", id));
        return mapToSiteResponse(site);
    }

    @Override
    @Transactional
    public SiteResponse updateSite(Long id, UpdateSiteRequest request, String performedBy) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site", "id", id));

        String newCode = request.getCode().trim().toUpperCase();
        if (!site.getCode().equalsIgnoreCase(newCode) &&
                siteRepository.existsByProjectIdAndCodeAndIdNot(site.getProject().getId(), newCode, id)) {
            throw new DuplicateResourceException("Site", "code", newCode + " under Project " + site.getProject().getCode());
        }

        site.setCode(newCode);
        site.setName(request.getName().trim());
        if (request.getSiteType() != null) site.setSiteType(validateAndNormalizeSiteType(request.getSiteType()));
        site.setLocation(request.getLocation());
        if (request.getStatus() != null) site.setStatus(validateAndNormalizeSiteStatus(request.getStatus()));
        site.setLatitude(request.getLatitude());
        site.setLongitude(request.getLongitude());
        site.setAreaSqFt(request.getAreaSqFt());
        site.setSiteIncharge(request.getSiteIncharge());

        Site updatedSite = siteRepository.save(site);

        auditService.logAction(
                performedBy,
                "UPDATE_SITE",
                "SITE",
                String.valueOf(updatedSite.getId()),
                "Updated site '" + updatedSite.getName() + "' (" + updatedSite.getCode() + ")",
                null
        );

        return mapToSiteResponse(updatedSite);
    }

    @Override
    @Transactional
    public SiteResponse updateSiteStatus(Long id, UpdateSiteStatusRequest request, String performedBy) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site", "id", id));

        String oldStatus = site.getStatus();
        String validatedStatus = validateAndNormalizeSiteStatus(request.getStatus());

        site.setStatus(validatedStatus);
        Site updatedSite = siteRepository.save(site);

        auditService.logAction(
                performedBy,
                "UPDATE_SITE_STATUS",
                "SITE",
                String.valueOf(id),
                "Changed site status of " + site.getName() + " from " + oldStatus + " to " + updatedSite.getStatus(),
                null
        );

        return mapToSiteResponse(updatedSite);
    }

    @Override
    @Transactional
    public void deleteSite(Long id, String performedBy) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site", "id", id));

        String siteName = site.getName();
        String siteCode = site.getCode();
        siteRepository.delete(site);

        auditService.logAction(
                performedBy,
                "DELETE_SITE",
                "SITE",
                String.valueOf(id),
                "Deleted site '" + siteName + "' (" + siteCode + ")",
                null
        );
    }

    private String validateAndNormalizeSiteStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new BadRequestException("Site status cannot be blank. Allowed statuses: " + ALLOWED_SITE_STATUSES);
        }
        String normalized = status.trim().toUpperCase();
        if (!ALLOWED_SITE_STATUSES.contains(normalized)) {
            throw new BadRequestException("Invalid site status: '" + status + "'. Allowed statuses are: " + ALLOWED_SITE_STATUSES);
        }
        return normalized;
    }

    private String validateAndNormalizeSiteType(String siteType) {
        if (siteType == null || siteType.trim().isEmpty()) {
            throw new BadRequestException("Site type cannot be blank. Allowed types: " + ALLOWED_SITE_TYPES);
        }
        String normalized = siteType.trim().toUpperCase();
        if (!ALLOWED_SITE_TYPES.contains(normalized)) {
            throw new BadRequestException("Invalid site type: '" + siteType + "'. Allowed types are: " + ALLOWED_SITE_TYPES);
        }
        return normalized;
    }

    private SiteResponse mapToSiteResponse(Site site) {
        return SiteResponse.builder()
                .id(site.getId())
                .projectId(site.getProject() != null ? site.getProject().getId() : null)
                .projectName(site.getProject() != null ? site.getProject().getName() : null)
                .code(site.getCode())
                .name(site.getName())
                .siteType(site.getSiteType())
                .location(site.getLocation())
                .status(site.getStatus())
                .latitude(site.getLatitude())
                .longitude(site.getLongitude())
                .areaSqFt(site.getAreaSqFt())
                .siteIncharge(site.getSiteIncharge())
                .createdAt(site.getCreatedAt())
                .updatedAt(site.getUpdatedAt())
                .build();
    }
}
