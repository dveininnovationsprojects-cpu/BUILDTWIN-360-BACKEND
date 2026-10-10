package com.example.BuildTwin._0.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "dpr_photo_attachments",
        indexes = {
                @Index(name = "idx_dpr_photo_dpr", columnList = "dpr_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DprPhotoAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dpr_id", nullable = false)
    @JsonIgnore
    private DailyProgressReport dpr;

    @Column(name = "photo_url", nullable = false, length = 500)
    private String photoUrl;

    @Column(name = "caption", length = 250)
    private String caption;

    @Column(name = "category", length = 50)
    @Builder.Default
    private String category = "PROGRESS";

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
