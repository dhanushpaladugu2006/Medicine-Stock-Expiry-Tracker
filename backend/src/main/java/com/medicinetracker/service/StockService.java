package com.medicinetracker.service;

import java.util.List;
import java.util.UUID;

import com.medicinetracker.dto.stock.StockAdjustmentRequest;
import com.medicinetracker.dto.stock.StockTransactionResponse;

public interface StockService {

    StockTransactionResponse adjustStock(StockAdjustmentRequest request);

    List<StockTransactionResponse> getHistory(UUID medicineId);
}
