package com.medicinetracker.dto.stock;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.medicinetracker.entity.enums.StockTransactionType;

public record StockTransactionResponse(
        UUID id,
        UUID medicineId,
        String medicineName,
        UUID branchId,
        String branchName,
        String performedBy,
        StockTransactionType type,
        Integer quantityBefore,
        Integer quantityChange,
        Integer quantityAfter,
        String referenceNote,
        BigDecimal unitPrice,
        OffsetDateTime transactionDate
) {
}
