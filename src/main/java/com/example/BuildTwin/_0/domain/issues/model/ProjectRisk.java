package com.example.BuildTwin._0.domain.issues.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "risks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Project Risk Register item with probability, impact, severity, owner and mitigation plan")
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

    @Column(name = "severity", length = 20) // LOW, MEDIUM, HIGH, CRITICAL
    private String severity;

    @Column(name = "risk_score")
    private Integer riskScore; // Numeric rating 1 to 9 (probability rating * impact rating)

    @Column(name = "category", length = 50) // TECHNICAL, COMMERCIAL, ENVIRONMENTAL, SAFETY, SCHEDULE
    private String category;

    @Column(name = "mitigation", columnDefinition = "TEXT")
    private String mitigation;

    @Column(name = "contingency_plan", columnDefinition = "TEXT")
    private String contingencyPlan;

    @Column(name = "owner", length = 100)
    private String owner; // Risk owner responsible for mitigation

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "IDENTIFIED"; // IDENTIFIED, MONITORED, MITIGATED, CLOSED

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "resolved_by", length = 100)
    private String resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper method to compute score and severity automatically based on probability & impact
    public void computeSeverityAndScore() {
        int probWeight = "HIGH".equalsIgnoreCase(this.probability) ? 3 : "MEDIUM".equalsIgnoreCase(this.probability) ? 2 : 1;
        int impactWeight = "HIGH".equalsIgnoreCase(this.impact) ? 3 : "MEDIUM".equalsIgnoreCase(this.impact) ? 2 : 1;
        this.riskScore = probWeight * impactWeight;
        if (this.severity == null || this.severity.isBlank()) {
            if (this.riskScore >= 6) {
                this.severity = "CRITICAL";
            } else if (this.riskScore >= 4) {
                this.severity = "HIGH";
            } else if (this.riskScore >= 2) {
                this.severity = "MEDIUM";
            } else {
                this.severity = "LOW";
            }
        }
    }
}
