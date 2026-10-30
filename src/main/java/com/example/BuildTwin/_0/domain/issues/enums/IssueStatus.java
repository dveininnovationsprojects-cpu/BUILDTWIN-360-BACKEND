package com.example.BuildTwin._0.domain.issues.enums;

public enum IssueStatus {
    OPEN,           // Newly identified & raised by Site Engineer / Quality Inspector
    ASSIGNED,       // Assigned by PM to a specific Contractor/Engineer with Target SLA Date
    IN_PROGRESS,    // Contractor started fixing the problem
    RESOLVED,       // Contractor completed rectification, uploaded proof photo & resolution remarks
    CLOSED          // Verified and approved by Quality Inspector / PM; if verification fails, reverts to OPEN
}
