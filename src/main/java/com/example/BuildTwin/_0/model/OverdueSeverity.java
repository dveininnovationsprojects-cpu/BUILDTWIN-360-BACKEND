package com.example.BuildTwin._0.model;

/**
 * Severity assessment of delay based on elapsed overdue days and downstream project blockage.
 */
public enum OverdueSeverity {
    CRITICAL,   // Over 30 days overdue OR blocking 2+ successor activities OR summary milestone task overdue
    HIGH,       // 15 - 30 days overdue OR blocking 1 successor activity
    MEDIUM,     // 8 - 14 days overdue
    LOW         // 1 - 7 days overdue
}
