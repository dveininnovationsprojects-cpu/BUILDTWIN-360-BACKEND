package com.example.BuildTwin._0.domain.notifications.enums;

public enum NotificationType {
    LOW_STOCK,           // Material balance fell below reorder level
    PROJECTED_SHORTAGE,  // Active demands exceed store stock
    CRITICAL_BLOCKER,    // Blocker halts site execution
    NCR_RAISED,          // Quality non-conformance recorded
    NCR_AUDIT_REJECTED,  // QA inspector rejected fix
    OVERDUE_SLA,         // Issue breached target resolution date
    BUDGET_OVERRUN,      // Cost head breached 80%/90%/100% threshold
    PO_APPROVED,         // Purchase order approved for delivery
    GRN_DEFECT_ALERT,    // Damaged goods received at gate
    PROJECT_STATUS_UPDATE,// Project lifecycle transition alerts
    GENERAL_INFO         // General project and operational status updates
}
