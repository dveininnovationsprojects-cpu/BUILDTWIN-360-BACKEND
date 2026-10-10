package com.example.BuildTwin._0.model;

/**
 * Lifecycle status of a Daily Progress Report (DPR).
 */
public enum DprStatus {
    /**
     * Initial draft created by site engineer / field supervisor, open for modification.
     */
    DRAFT,

    /**
     * Submitted by site engineer for managerial review and sign-off.
     */
    SUBMITTED,

    /**
     * Approved by Project Manager or Director; progress quantities cascade into active WBS activities.
     */
    APPROVED,

    /**
     * Rejected by reviewer with documented feedback for correction.
     */
    REJECTED
}
