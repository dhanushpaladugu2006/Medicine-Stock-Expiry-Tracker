package com.medicinetracker.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.medicinetracker.entity.Medicine;
import com.medicinetracker.entity.enums.MedicineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicineRepository extends JpaRepository<Medicine, UUID>, JpaSpecificationExecutor<Medicine> {

    Optional<Medicine> findByIdAndArchivedFalse(UUID id);

    Optional<Medicine> findByBranchIdAndBatchNumberIgnoreCase(UUID branchId, String batchNumber);

    long countByArchivedFalse();

    long countByArchivedFalseAndStatus(MedicineStatus status);

    long countByArchivedFalseAndQuantityLessThanEqual(int quantity);

    long countByArchivedFalseAndQuantity(int quantity);

    List<Medicine> findByArchivedFalseAndExpiryDateLessThanEqual(LocalDate thresholdDate);

    @Query("""
            select m from Medicine m
            where m.archived = false
              and m.branch.id = :branchId
              and m.quantity <= m.reorderLevel
            """)
    List<Medicine> findLowStockByBranch(@Param("branchId") UUID branchId);

    @Query("""
            select m from Medicine m
            where m.archived = false
              and m.expiryDate between :fromDate and :toDate
            order by m.expiryDate asc
            """)
    List<Medicine> findExpiringBetween(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);
}
