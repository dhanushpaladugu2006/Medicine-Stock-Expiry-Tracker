package com.medicinetracker.dto.stock;

import java.math.BigDecimal;
import java.util.UUID;

import com.medicinetracker.entity.enums.StockTransactionType;
import jakarta.validation.constraints.NotNull;

public record StockAdjustmentRequest(
        @NotNull UUID medicineId,
        @NotNull Integer quantityChange,
        @NotNull StockTransactionType type,
        String referenceNote,
        BigDecimal unitPrice
) {
}
