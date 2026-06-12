package com.medicinetracker.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.medicinetracker.entity.StockTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface StockTransactionRepository extends MongoRepository<StockTransaction, UUID> {

    List<StockTransaction> findTop20ByMedicineIdOrderByTransactionDateDesc(UUID medicineId);

    @Query(value = "{ 'transactionDate': { $gte: ?0, $lte: ?1 } }", sort = "{ 'transactionDate': -1 }")
    List<StockTransaction> findByDateRange(OffsetDateTime fromDate, OffsetDateTime toDate);

    List<StockTransaction> findByMedicineIdAndTransactionDateGreaterThanEqual(UUID medicineId, OffsetDateTime fromDate);
}
