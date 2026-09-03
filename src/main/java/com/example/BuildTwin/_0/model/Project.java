package com.example.BuildTwin._0.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "project_type")
    private String projectType; // RESIDENTIAL, COMMERCIAL, INFRASTRUCTURE, INDUSTRIAL

    @Column(name = "location")
    private String location;

    @Column(name = "status")
    private String status; // PLANNED, ACTIVE, ON_HOLD, COMPLETED, ARCHIVED

    @Column(name = "planned_start_date")
    private LocalDate plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDate plannedEndDate;

    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Column(name = "estimated_budget", precision = 15, scale = 2)
    private BigDecimal estimatedBudget;

    @Column(name = "currency")
    @Builder.Default
    private String currency = "INR";

    @Column(name = "total_built_up_area_sqft")
    private Double totalBuiltUpAreaSqFt;

    @Column(name = "project_manager_id")
    private Long projectManagerId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<Site> sites = new ArrayList<>();
}
