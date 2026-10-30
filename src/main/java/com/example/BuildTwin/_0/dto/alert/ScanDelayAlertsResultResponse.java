package com.example.BuildTwin._0.dto.alert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanDelayAlertsResultResponse {

    private Long projectId;
    private String projectCode;
    private String projectName;
    private LocalDate asOfDate;

    private Integer totalActivitiesScanned;
    private Integer newAlertsCreated;
    private Integer existingAlertsUpdated;
    private Integer alertsAutoResolved;
    private Integer totalActiveAlerts;

    private LocalDateTime scanTimestamp;
    private String summary;
    private List<ScheduleDelayAlertResponse> alerts;
}
