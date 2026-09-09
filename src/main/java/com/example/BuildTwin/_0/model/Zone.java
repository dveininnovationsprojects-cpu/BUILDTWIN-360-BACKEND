package com.example.BuildTwin._0.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "zones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id", nullable = false)
    @JsonIgnore
    private Floor floor;

    @Column(name = "code", nullable = false)
    private String code; // e.g. "ZN-FL1-A", "FLAT-101", "LIFT-LOBBY"

    @Column(name = "name", nullable = false)
    private String name; // e.g. "Zone A (Flats 101-104)", "Main Electrical Room"

    @Column(name = "zone_type")
    private String zoneType; // RESIDENTIAL_UNIT, COMMON_AREA, CORRIDOR, ELECTRICAL_ROOM, DUCT_SHAFT, STAIRCASE

    @Column(name = "area_sqft")
    private Double areaSqFt;

    @Column(name = "status")
    private String status; // PLANNED, IN_PROGRESS, COMPLETED

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
