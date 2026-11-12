package com.example.BuildTwin._0.dto.integration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Activity Cross-Module Entity Reference Mapping")
public class ActivityCrossReferenceDto {

    @Schema(description = "Activity ID", example = "101")
    private Long activityId;

    @Schema(description = "Activity Code", example = "ACT-CIV-001")
    private String activityCode;

    @Schema(description = "Activity Name", example = "Substructure Concrete Pouring")
    private String activityName;

    @Schema(description = "Activity Status", example = "IN_PROGRESS")
    private String activityStatus;

    @Schema(description = "Discipline", example = "CIVIL")
    private String discipline;

    @Schema(description = "Parent Work Package ID", example = "1")
    private Long workPackageId;

    @Schema(description = "Parent Work Package Code", example = "WP-CIVIL-01")
    private String workPackageCode;

    @Schema(description = "Parent Work Package Name", example = "Foundation Works")
    private String workPackageName;

    @Schema(description = "Linked Site ID", example = "10")
    private Long siteId;

    @Schema(description = "Linked Site Name", example = "North Tower Site")
    private String siteName;

    @Schema(description = "Linked Building Name", example = "Tower A")
    private String buildingName;

    @Schema(description = "Linked Floor Name", example = "Ground Floor")
    private String floorName;

    @Schema(description = "Linked Zone ID", example = "105")
    private Long zoneId;

    @Schema(description = "Linked Zone Name", example = "Zone B - West Wing")
    private String zoneName;

    @Schema(description = "Actual progress percentage logged", example = "65.0")
    private Double progressPercentage;

    @Schema(description = "Assigned contractor or gang", example = "L&T Heavy Civil")
    private String assignedContractor;

    @Schema(description = "Cross-module synchronization state (SYNCED, PENDING, UNLINKED)", example = "SYNCED")
    private String syncStatus;
}
