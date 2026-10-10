package com.example.BuildTwin._0.model;

/**
 * Classification of overdue nature for construction activities.
 */
public enum OverdueType {
    OVERDUE_FINISH,     // Planned end date has passed, but activity is not yet completed
    OVERDUE_START,      // Planned start date has passed, but activity has not commenced (status is PLANNED / 0% progress)
    BEHIND_SCHEDULE     // Activity is in progress, but completion rate lags significantly behind elapsed planned duration
}
