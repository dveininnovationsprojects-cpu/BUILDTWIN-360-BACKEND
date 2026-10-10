package com.example.BuildTwin._0.model;

/**
 * Classification of Schedule Delay Alert types in construction project management.
 */
public enum DelayAlertType {
    /**
     * Activity has missed its planned finish date and physical work is incomplete.
     */
    OVERDUE_FINISH,

    /**
     * Activity has passed its planned start date but site work has not commenced (0% progress or status PLANNED).
     */
    OVERDUE_START,

    /**
     * Activity is underway, but physical completion rate lags behind elapsed planned time by a significant threshold (e.g. >= 20%).
     */
    PROGRESS_SLIPPAGE,

    /**
     * Activity on the Critical Path is slipping or halted, directly threatening project milestone or final handover date.
     */
    CRITICAL_PATH_AT_RISK,

    /**
     * Delays on this predecessor activity are blocking or cascading into multiple downstream successor tasks.
     */
    SUCCESSOR_IMPACT_CASCADE,

    /**
     * Early Warning: Activity scheduled to start or finish within the near-term horizon (e.g. 3-7 days) with uncleared constraints or pending predecessors.
     */
    UPCOMING_DEADLINE_RISK
}
