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
@Table(name = "floors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Floor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    @JsonIgnore
    private Building building;

    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber; // e.g. -2, -1, 0 (Ground/Stilt), 1, 2, ... 18

    @Column(name = "floor_name", nullable = false)
    private String floorName; // e.g. "Basement 1", "Stilt Floor", "First Floor", "Terrace Floor"

    @Column(name = "floor_type")
    private String floorType; // BASEMENT, STILT, PODIUM, TYPICAL, REFUGE, TERRACE

    @Column(name = "built_up_area_sqft")
    private Double builtUpAreaSqFt;

    @Column(name = "status")
    private String status; // PLANNED, IN_PROGRESS, COMPLETED

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "floor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<Zone> zones = new ArrayList<>();
}
