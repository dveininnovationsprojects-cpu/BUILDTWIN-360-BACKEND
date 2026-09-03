package com.example.BuildTwin._0.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private Project project;

    @Column(name = "code", nullable = false)
    private String code; // e.g. "PADUR-TWR-A"

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "site_type")
    private String siteType; // TOWER, INFRASTRUCTURE, CLUBHOUSE, VILLA_ZONE, UTILITIES

    @Column(name = "location")
    private String location;

    @Column(name = "status")
    private String status; // ACTIVE, INACTIVE, COMPLETED

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "area_sqft")
    private Double areaSqFt;

    @Column(name = "site_incharge")
    private String siteIncharge;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
