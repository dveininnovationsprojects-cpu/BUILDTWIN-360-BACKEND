package com.example.BuildTwin._0.dto.hierarchy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Full recursive Digital Twin Physical Hierarchy Tree (Project -> Sites -> Buildings -> Floors -> Zones)")
public class HierarchyTreeResponse {

    private Long id;
    private String name;
    private String code;
    private String type; // PROJECT
    private String status;

    @Builder.Default
    private List<SiteNode> sites = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SiteNode {
        private Long id;
        private String name;
        private String code;
        private String siteType;
        private String status;
        private Double latitude;
        private Double longitude;
        @Builder.Default
        private List<BuildingNode> buildings = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BuildingNode {
        private Long id;
        private String name;
        private String code;
        private String buildingType;
        private Integer totalFloors;
        private String status;
        @Builder.Default
        private List<FloorNode> floors = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FloorNode {
        private Long id;
        private Integer floorNumber;
        private String floorName;
        private String floorType;
        private String status;
        @Builder.Default
        private List<ZoneNode> zones = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ZoneNode {
        private Long id;
        private String code;
        private String name;
        private String zoneType;
        private Double areaSqFt;
        private String status;
    }
}
