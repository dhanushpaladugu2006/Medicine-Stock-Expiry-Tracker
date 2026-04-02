package com.medicinetracker.service.impl;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.medicinetracker.dto.dashboard.DashboardChartPointResponse;
import com.medicinetracker.dto.dashboard.DashboardSummaryResponse;
import com.medicinetracker.dto.dashboard.PredictionInsightResponse;
import com.medicinetracker.entity.Medicine;
import com.medicinetracker.entity.StockTransaction;
import com.medicinetracker.entity.User;
import com.medicinetracker.entity.enums.Role;
import com.medicinetracker.repository.MedicineRepository;
import com.medicinetracker.repository.StockTransactionRepository;
import com.medicinetracker.service.DashboardService;
import com.medicinetracker.util.AuthenticatedUserProvider;
import com.medicinetracker.util.MedicineSpecifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final MedicineRepository medicineRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public DashboardServiceImpl(MedicineRepository medicineRepository, StockTransactionRepository stockTransactionRepository, AuthenticatedUserProvider authenticatedUserProvider) {
        this.medicineRepository = medicineRepository;
        this.stockTransactionRepository = stockTransactionRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Override
    public DashboardSummaryResponse getSummary() {
        User user = authenticatedUserProvider.getCurrentUser();
        UUID branchId = user.getRole() == Role.ADMIN || user.getBranch() == null ? null : user.getBranch().getId();
        List<Medicine> medicines = medicineRepository.findAll(MedicineSpecifications.filter(null, null, branchId, null, null, null));
        LocalDate today = LocalDate.now();

        long expiringIn7Days = medicines.stream().filter(m -> !m.getExpiryDate().isBefore(today) && !m.getExpiryDate().isAfter(today.plusDays(7))).count();
        long expiringIn15Days = medicines.stream().filter(m -> !m.getExpiryDate().isBefore(today) && !m.getExpiryDate().isAfter(today.plusDays(15))).count();
        long expiringIn30Days = medicines.stream().filter(m -> !m.getExpiryDate().isBefore(today) && !m.getExpiryDate().isAfter(today.plusDays(30))).count();
        long lowStockCount = medicines.stream().filter(m -> m.getQuantity() > 0 && m.getQuantity() <= m.getReorderLevel()).count();
        long outOfStockCount = medicines.stream().filter(m -> m.getQuantity() <= 0).count();

        List<DashboardChartPointResponse> expiryTrend = List.of(
                new DashboardChartPointResponse("7 days", expiringIn7Days),
                new DashboardChartPointResponse("15 days", expiringIn15Days),
                new DashboardChartPointResponse("30 days", expiringIn30Days)
        );

        OffsetDateTime from = OffsetDateTime.now().minusWeeks(5);
        List<StockTransaction> transactions = stockTransactionRepository.findByDateRange(from, OffsetDateTime.now());
        List<DashboardChartPointResponse> stockTrend = new ArrayList<>();
        for (int weekOffset = 5; weekOffset >= 0; weekOffset--) {
            OffsetDateTime weekStart = OffsetDateTime.now().minusWeeks(weekOffset).withHour(0).withMinute(0).withSecond(0).withNano(0);
            OffsetDateTime weekEnd = weekStart.plusDays(6).withHour(23).withMinute(59);
            long totalMovement = transactions.stream()
                    .filter(tx -> !tx.getTransactionDate().isBefore(weekStart) && !tx.getTransactionDate().isAfter(weekEnd))
                    .mapToLong(tx -> Math.abs(tx.getQuantityChange()))
                    .sum();
            stockTrend.add(new DashboardChartPointResponse("W" + (6 - weekOffset), totalMovement));
        }

        List<PredictionInsightResponse> predictions = medicines.stream()
                .map(this::toPrediction)
                .sorted(Comparator.comparingLong(PredictionInsightResponse::estimatedDaysToExpiry))
                .limit(5)
                .toList();

        return new DashboardSummaryResponse(
                medicines.size(),
                expiringIn7Days,
                expiringIn15Days,
                expiringIn30Days,
                lowStockCount,
                outOfStockCount,
                expiryTrend,
                stockTrend,
                predictions
        );
    }

    private PredictionInsightResponse toPrediction(Medicine medicine) {
        Integer consumption = stockTransactionRepository.totalConsumptionSince(medicine.getId(), OffsetDateTime.now().minusDays(30));
        int monthlyConsumption = consumption == null ? 0 : consumption;
        long estimatedDaysToExhaust = monthlyConsumption <= 0 ? 999 : Math.max(1, Math.round((double) medicine.getQuantity() / (monthlyConsumption / 30.0)));
        long estimatedDaysToExpiry = Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), medicine.getExpiryDate()));
        String recommendation = estimatedDaysToExpiry < estimatedDaysToExhaust
                ? "Promote, transfer, or bundle this stock before expiry."
                : "Current usage trend is healthy. Keep monitoring weekly.";
        return new PredictionInsightResponse(
                medicine.getId(),
                medicine.getName(),
                medicine.getBatchNumber(),
                estimatedDaysToExhaust,
                estimatedDaysToExpiry,
                recommendation
        );
    }
}

