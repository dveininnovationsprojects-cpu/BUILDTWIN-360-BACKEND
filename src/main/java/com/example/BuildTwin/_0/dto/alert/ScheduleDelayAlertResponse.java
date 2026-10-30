package com.example.BuildTwin._0.dto.alert;

import com.example.BuildTwin._0.model.DelayAlertStatus;
import com.example.BuildTwin._0.model.DelayAlertType;
import com.example.BuildTwin._0.model.OverdueAgingBucket;
import com.example.BuildTwin._0.model.OverdueSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.example.BuildTwin._0.domain.projects.model.Project;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleDelayAlertResponse {

    private Long id;
    private String alertCode;

    // Project metadata
    private Long projectId;
    private String projectCode;
    private String projectName;

    // Activity metadata
    private Long activityId;
    private String activityCode;
    private String activityName;
    private String discipline;
    private Integer level;
    private String wbsPath;
    private String workPackageCode;
    private String workPackageName;
    private String siteName;
    private String buildingName;
    private String floorName;
    private String zoneName;

    // Alert attributes
    private DelayAlertType alertType;
    private OverdueSeverity severity;
    private DelayAlertStatus status;
    private OverdueAgingBucket agingBucket;
    private Long delayDays;
    private Double progressSlippagePercentage;
    private Double currentProgressPercentage;
    private Double expectedProgressPercentage;

    // Activity dates & quantities
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;
    private Double plannedQuantity;
    private Double completedQuantity;
    private Double remainingQuantity;
    private String uom;
    private String activityStatus;
    private String assignedContractor;

    // Impact & successors
    private Integer impactedSuccessorCount;
    private String impactedSuccessorCodes;

    // Descriptions
    private String title;
    private String description;
    private String recommendedAction;

    // Acknowledgment
    private Boolean isAcknowledged;
    private String acknowledgedBy;
    private LocalDateTime acknowledgedAt;
    private String acknowledgmentNotes;
    private LocalDate targetResolutionDate;

    // Mitigation
    private String mitigationAction;
    private String delayReasonCategory;
    private String delayReasonDetails;
    private Integer additionalLaborAllocated;

    // Resolution
    private Boolean isResolved;
    private String resolvedBy;
    private LocalDateTime resolvedAt;
    private String resolutionNotes;

    // Dismissal
    private Boolean isDismissed;
    private String dismissedBy;
    private LocalDateTime dismissedAt;
    private String dismissalReason;

    // Timestamps
    private LocalDateTime triggeredAt;
    private LocalDateTime lastEvaluatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
