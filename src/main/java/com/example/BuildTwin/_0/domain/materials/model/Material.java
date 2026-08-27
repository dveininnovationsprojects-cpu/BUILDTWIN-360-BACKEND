package com.example.BuildTwin._0.domain.materials.model;

import com.example.BuildTwin._0.domain.materials.enums.MaterialUnit;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "materials", indexes = {
        @Index(name = "idx_material_code", columnList = "material_code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Material code is required")
    @Column(name = "material_code", nullable = false, unique = true, length = 50)
    private String materialCode;

    @NotBlank(message = "Material name is required")
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "category", length = 100)
    private String category;

    @NotNull(message = "Material unit is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false, length = 30)
    private MaterialUnit unit;

    @NotNull(message = "Standard rate is required")
    @Positive(message = "Standard rate must be positive")
    @Column(name = "standard_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal standardRate;

    @Column(name = "reorder_level", precision = 12, scale = 2)
    private BigDecimal reorderLevel;

    @Builder.Default
    @Min(value = 0, message = "Current stock cannot be negative")
    @Column(name = "current_stock", nullable = false, precision = 12, scale = 2)
    private BigDecimal currentStock = BigDecimal.ZERO;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
