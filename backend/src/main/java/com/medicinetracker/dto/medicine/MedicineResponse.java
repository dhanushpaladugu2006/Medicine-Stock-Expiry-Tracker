package com.medicinetracker.dto.medicine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.medicinetracker.entity.enums.MedicineStatus;

public record MedicineResponse(
        UUID id,
        String name,
        String batchNumber,
        String category,
        String manufacturer,
        Integer quantity,
        Integer reorderLevel,
        BigDecimal price,
        LocalDate expiryDate,
        LocalDate manufactureDate,
        String barcode,
        String imageUrl,
        MedicineStatus status,
        UUID branchId,
        String branchName,
        boolean lowStock,
        long predictedExpiryRiskScore
) {
}
