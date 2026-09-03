package com.example.BuildTwin._0.domain.dpr.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "dpr_photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DprPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "DPR Header ID is required")
    @Column(name = "dpr_header_id", nullable = false)
    private Long dprHeaderId;

    @Column(name = "activity_id")
    private Long activityId;

    @NotBlank(message = "Photo object URL is required")
    @Column(name = "object_url", nullable = false, length = 255)
    private String objectUrl;

    @Column(name = "tags", length = 150)
    private String tags;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
