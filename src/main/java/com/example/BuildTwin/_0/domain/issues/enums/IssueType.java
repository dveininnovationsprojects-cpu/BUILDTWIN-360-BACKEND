package com.example.BuildTwin._0.domain.issues.enums;

public enum IssueType {
    SNAG,       // Minor defect (e.g., paint peeling, tile crack)
    NCR,        // Non-Conformance Report (Major quality failure e.g., concrete cube test fail)
    ISSUE,      // General operational problem (e.g., crane breakdown, water cut)
    RISK,       // Future potential problem (e.g., heavy rain forecast, excavation hazard)
    BLOCKER     // Site work completely halted (e.g., villagers strike, permit pending)
}
