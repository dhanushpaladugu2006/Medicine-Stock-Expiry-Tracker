package com.medicinetracker.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.medicinetracker.entity.Medicine;
import com.medicinetracker.entity.enums.MedicineStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface MedicineRepository extends MongoRepository<Medicine, UUID> {

    Optional<Medicine> findByIdAndArchivedFalse(UUID id);

    Optional<Medicine> findByBranchIdAndBatchNumberIgnoreCase(UUID branchId, String batchNumber);

    long countByArchivedFalse();

    long countByArchivedFalseAndStatus(MedicineStatus status);

    long countByArchivedFalseAndQuantityLessThanEqual(int quantity);

    long countByArchivedFalseAndQuantity(int quantity);

    List<Medicine> findByArchivedFalseAndExpiryDateLessThanEqual(LocalDate thresholdDate);

    @Query("{ 'archived': false, 'branch': ?0, '$expr': { '$lte': [ '$quantity', '$reorderLevel' ] } }")
    List<Medicine> findLowStockByBranch(UUID branchId);

    @Query(value = "{ 'archived': false, 'expiryDate': { $gte: ?0, $lte: ?1 } }", sort = "{ 'expiryDate': 1 }")
    List<Medicine> findExpiringBetween(LocalDate fromDate, LocalDate toDate);
}
