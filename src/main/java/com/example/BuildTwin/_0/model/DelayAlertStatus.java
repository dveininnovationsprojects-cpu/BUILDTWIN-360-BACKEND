package com.example.BuildTwin._0.model;

/**
 * Lifecycle states of a Schedule Delay Alert.
 */
public enum DelayAlertStatus {
    /**
     * Alert newly detected or recurring, awaiting review and intervention by project team.
     */
    ACTIVE,

    /**
     * Alert reviewed and acknowledged by site engineer or project manager, target resolution date established.
     */
    ACKNOWLEDGED,

    /**
     * Formal catch-up recovery action plan recorded and actively being executed on site.
     */
    IN_MITIGATION,

    /**
     * Delay has been successfully eliminated, activity completed, or schedule formally revised.
     */
    RESOLVED,

    /**
     * Alert dismissed or suppressed with documented justification (e.g. scope de-prioritized or false positive).
     */
    DISMISSED
}
