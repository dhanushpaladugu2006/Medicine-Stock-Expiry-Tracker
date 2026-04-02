package com.medicinetracker.dto.dashboard;

import java.util.List;

public record DashboardSummaryResponse(
        long totalMedicines,
        long expiringIn7Days,
        long expiringIn15Days,
        long expiringIn30Days,
        long lowStockCount,
        long outOfStockCount,
        List<DashboardChartPointResponse> expiryTrend,
        List<DashboardChartPointResponse> stockTrend,
        List<PredictionInsightResponse> predictions
) {
}
