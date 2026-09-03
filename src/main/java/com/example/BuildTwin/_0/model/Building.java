package com.example.BuildTwin._0.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "buildings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    @JsonIgnore
    private Site site;

    @Column(name = "code", nullable = false)
    private String code; // e.g. "BLD-TWR-A"

    @Column(name = "name", nullable = false)
    private String name; // e.g. "Tower A - Residential"

    @Column(name = "building_type")
    private String buildingType; // RESIDENTIAL_TOWER, COMMERCIAL_BLOCK, CLUBHOUSE, PODIUM, PARKING_BLOCK

    @Column(name = "total_floors")
    private Integer totalFloors; // e.g. 18

    @Column(name = "total_built_up_area_sqft")
    private Double totalBuiltUpAreaSqFt;

    @Column(name = "status")
    private String status; // PLANNED, UNDER_CONSTRUCTION, COMPLETED, ON_HOLD

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<Floor> floors = new ArrayList<>();
}
