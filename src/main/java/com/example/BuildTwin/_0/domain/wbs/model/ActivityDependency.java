package com.example.BuildTwin._0.domain.wbs.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "activity_dependencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Predecessor activity ID is required")
    @Column(name = "predecessor_id", nullable = false)
    private Long predecessorId;

    @NotNull(message = "Successor activity ID is required")
    @Column(name = "successor_id", nullable = false)
    private Long successorId;

    @Builder.Default
    @Column(name = "type", length = 20)
    private String type = "FS"; // Finish-to-Start

    @Builder.Default
    @Column(name = "lag")
    private Integer lag = 0;
}
