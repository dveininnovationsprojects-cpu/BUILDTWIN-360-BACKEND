package com.example.BuildTwin._0.model;

/**
 * Standard Construction Precedence Network Dependency Types
 * (PMBOK / Primavera P6 / MS Project compliant)
 */
public enum DependencyType {
    /**
     * Finish-to-Start (FS):
     * The successor activity cannot start until the predecessor activity has finished.
     * Most common dependency type in construction (e.g., Concrete Curing finishes -> Formwork Removal starts).
     */
    FS("Finish-to-Start"),

    /**
     * Start-to-Start (SS):
     * The successor activity cannot start until the predecessor activity has started.
     * Used for concurrent / fast-tracking activities (e.g., Pipe trenching starts -> Pipe laying can start).
     */
    SS("Start-to-Start"),

    /**
     * Finish-to-Finish (FF):
     * The successor activity cannot finish until the predecessor activity has finished.
     * Used for parallel activities that wrap up together (e.g., HVAC ducting must finish before Ceiling closure finishes).
     */
    FF("Finish-to-Finish"),

    /**
     * Start-to-Finish (SF):
     * The successor activity cannot finish until the predecessor activity has started.
     * Rarely used in construction, occasionally for shift handovers / security watch.
     */
    SF("Start-to-Finish");

    private final String displayName;

    DependencyType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DependencyType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return FS;
        }
        for (DependencyType type : values()) {
            if (type.name().equalsIgnoreCase(value.trim()) || type.displayName.equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid DependencyType: '" + value + "'. Allowed values: FS, SS, FF, SF");
    }
}
