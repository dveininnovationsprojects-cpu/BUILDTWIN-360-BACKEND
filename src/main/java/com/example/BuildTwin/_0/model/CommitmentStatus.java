package com.example.BuildTwin._0.model;

/**
 * Commitment and execution status for an activity in the Weekly Work Plan / Look-Ahead schedule.
 */
public enum CommitmentStatus {
    COMMITTED,          // Work package/task committed for execution in this lookahead week
    PENDING_READINESS,  // Activity scheduled but pending prerequisite constraints (materials/drawings/access)
    BLOCKED,            // Hindrance detected; work cannot proceed
    COMPLETED,          // Weekly target fulfilled
    NOT_ACHIEVED        // Weekly target missed
}
