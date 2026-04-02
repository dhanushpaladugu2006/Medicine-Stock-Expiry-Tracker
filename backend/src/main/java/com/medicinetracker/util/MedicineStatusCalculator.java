package com.medicinetracker.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.medicinetracker.entity.Medicine;
import com.medicinetracker.entity.enums.MedicineStatus;

public final class MedicineStatusCalculator {

    private MedicineStatusCalculator() {
    }

    public static MedicineStatus calculate(Medicine medicine) {
        if (medicine.getQuantity() <= 0) {
            return MedicineStatus.OUT_OF_STOCK;
        }

        long daysToExpiry = ChronoUnit.DAYS.between(LocalDate.now(), medicine.getExpiryDate());
        if (daysToExpiry < 0) {
            return MedicineStatus.EXPIRED;
        }
        if (daysToExpiry <= 7) {
            return MedicineStatus.NEAR_EXPIRY_7;
        }
        if (daysToExpiry <= 15) {
            return MedicineStatus.NEAR_EXPIRY_15;
        }
        if (daysToExpiry <= 30) {
            return MedicineStatus.NEAR_EXPIRY_30;
        }
        return MedicineStatus.SAFE;
    }
}
