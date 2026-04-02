package com.medicinetracker.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.medicinetracker.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, UUID> {

    List<StockTransaction> findTop20ByMedicineIdOrderByTransactionDateDesc(UUID medicineId);

    @Query("""
            select st from StockTransaction st
            where st.transactionDate between :fromDate and :toDate
            order by st.transactionDate desc
            """)
    List<StockTransaction> findByDateRange(@Param("fromDate") OffsetDateTime fromDate, @Param("toDate") OffsetDateTime toDate);

    @Query("""
            select coalesce(sum(case when st.quantityChange < 0 then abs(st.quantityChange) else 0 end), 0)
            from StockTransaction st
            where st.medicine.id = :medicineId
              and st.transactionDate >= :fromDate
            """)
    Integer totalConsumptionSince(@Param("medicineId") UUID medicineId, @Param("fromDate") OffsetDateTime fromDate);
}
