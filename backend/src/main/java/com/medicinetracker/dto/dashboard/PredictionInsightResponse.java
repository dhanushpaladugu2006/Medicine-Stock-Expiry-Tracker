package com.medicinetracker.dto.dashboard;

import java.util.UUID;

public record PredictionInsightResponse(
        UUID medicineId,
        String medicineName,
        String batchNumber,
        long estimatedDaysToExhaust,
        long estimatedDaysToExpiry,
        String recommendation
) {
}
