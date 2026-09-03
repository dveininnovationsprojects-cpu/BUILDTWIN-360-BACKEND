package com.example.BuildTwin._0.domain.equipment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Asset code is required")
    @Column(name = "asset_code", nullable = false, unique = true, length = 50)
    private String assetCode;

    @NotBlank(message = "Equipment name is required")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "type", length = 50) // MIXER, VIBRATOR, GENERATOR, SCAFFOLDING, CRANE
    private String type;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "AVAILABLE"; // AVAILABLE, IN_USE, MAINTENANCE

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
