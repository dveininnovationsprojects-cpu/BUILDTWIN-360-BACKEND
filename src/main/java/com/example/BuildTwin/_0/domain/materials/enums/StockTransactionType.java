package com.example.BuildTwin._0.domain.materials.enums;

public enum StockTransactionType {
    RECEIPT,     // Inward material received from supplier/GRN
    ISSUE,       // Material issued to site/subcontractor
    CONSUMPTION, // Material consumed on site work
    WASTAGE,     // Material wastage on site
    RETURN,      // Material returned to store/supplier
    ADJUSTMENT   // Inventory stock audit reconciliation adjustment
}
