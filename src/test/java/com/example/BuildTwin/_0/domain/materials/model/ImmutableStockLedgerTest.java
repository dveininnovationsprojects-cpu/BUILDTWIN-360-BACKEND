package com.example.BuildTwin._0.domain.materials.model;

import com.example.BuildTwin._0.domain.materials.enums.MaterialUnit;
import com.example.BuildTwin._0.domain.materials.enums.StockTransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ImmutableStockLedgerTest {

    @Test
    @DisplayName("Should throw UnsupportedOperationException when preUpdate hook is invoked")
    void testPreUpdateThrowsException() {
        Material material = Material.builder()
                .id(1L)
                .materialCode("MAT-CEM-001")
                .name("OPC Cement")
                .unit(MaterialUnit.BAGS)
                .standardRate(new BigDecimal("380.00"))
                .currentStock(new BigDecimal("100.00"))
                .build();

        StockLedger entry = StockLedger.builder()
                .id(10L)
                .projectId(101L)
                .activityId(301L)
                .material(material)
                .transactionType(StockTransactionType.RECEIPT)
                .quantity(new BigDecimal("50.00"))
                .unitPrice(new BigDecimal("380.00"))
                .build();

        UnsupportedOperationException updateEx = assertThrows(UnsupportedOperationException.class, entry::onPreUpdate);
        assertEquals("Stock ledger entries are immutable and cannot be updated.", updateEx.getMessage());
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException when preRemove hook is invoked")
    void testPreRemoveThrowsException() {
        Material material = Material.builder()
                .id(1L)
                .materialCode("MAT-STEEL-002")
                .name("TMT Steel Bars 12mm")
                .unit(MaterialUnit.TONNES)
                .standardRate(new BigDecimal("65000.00"))
                .currentStock(new BigDecimal("20.00"))
                .build();

        StockLedger entry = StockLedger.builder()
                .id(11L)
                .projectId(101L)
                .activityId(302L)
                .material(material)
                .transactionType(StockTransactionType.CONSUMPTION)
                .quantity(new BigDecimal("5.00"))
                .unitPrice(new BigDecimal("65000.00"))
                .build();

        UnsupportedOperationException removeEx = assertThrows(UnsupportedOperationException.class, entry::onPreRemove);
        assertEquals("Stock ledger entries are immutable and cannot be deleted.", removeEx.getMessage());
    }
}
