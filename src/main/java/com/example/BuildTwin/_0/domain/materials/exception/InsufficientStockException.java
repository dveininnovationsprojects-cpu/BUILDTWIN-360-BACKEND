package com.example.BuildTwin._0.domain.materials.exception;

import com.example.BuildTwin._0.exception.BuildTwinException;

public class InsufficientStockException extends BuildTwinException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(Long materialId, String materialName, double requestedQty, double availableQty) {
        super(String.format("Insufficient stock for material '%s' (ID: %d). Requested: %.2f, Available: %.2f",
                materialName, materialId, requestedQty, availableQty));
    }
}
