package com.medicinetracker.controller;

import java.util.List;
import java.util.UUID;

import com.medicinetracker.dto.common.ApiResponse;
import com.medicinetracker.dto.stock.StockAdjustmentRequest;
import com.medicinetracker.dto.stock.StockTransactionResponse;
import com.medicinetracker.service.StockService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stocks")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping("/adjustments")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> adjust(@Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stock adjusted", stockService.adjustStock(request)));
    }

    @GetMapping("/medicines/{medicineId}/history")
    public ResponseEntity<ApiResponse<List<StockTransactionResponse>>> history(@PathVariable UUID medicineId) {
        return ResponseEntity.ok(ApiResponse.success("Stock history loaded", stockService.getHistory(medicineId)));
    }
}

