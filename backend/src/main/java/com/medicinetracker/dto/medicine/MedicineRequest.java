package com.medicinetracker.dto.medicine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MedicineRequest(
        @NotBlank String name,
        @NotBlank String batchNumber,
        @NotBlank String category,
        @NotBlank String manufacturer,
        @NotNull @Min(0) Integer quantity,
        @NotNull @Min(0) Integer reorderLevel,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @NotNull LocalDate expiryDate,
        @NotNull LocalDate manufactureDate,
        String barcode,
        UUID branchId
) {
}
