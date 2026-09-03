package com.example.BuildTwin._0.domain.issues.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "risks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRisk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Project ID is required")
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "risk_title", nullable = false, length = 150)
    private String riskTitle;

    @Column(name = "probability", length = 20) // LOW, MEDIUM, HIGH
    private String probability;

    @Column(name = "impact", length = 20) // LOW, MEDIUM, HIGH
    private String impact;

    @Column(name = "mitigation", columnDefinition = "TEXT")
    private String mitigation;

    @Column(name = "owner", length = 100)
    private String owner;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "IDENTIFIED";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
