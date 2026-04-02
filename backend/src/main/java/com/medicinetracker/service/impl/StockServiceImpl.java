package com.medicinetracker.service.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.medicinetracker.dto.stock.StockAdjustmentRequest;
import com.medicinetracker.dto.stock.StockTransactionResponse;
import com.medicinetracker.entity.Medicine;
import com.medicinetracker.entity.StockTransaction;
import com.medicinetracker.entity.User;
import com.medicinetracker.entity.enums.AuditAction;
import com.medicinetracker.entity.enums.NotificationType;
import com.medicinetracker.mapper.StockTransactionMapper;
import com.medicinetracker.repository.MedicineRepository;
import com.medicinetracker.repository.StockTransactionRepository;
import com.medicinetracker.service.AuditService;
import com.medicinetracker.service.NotificationService;
import com.medicinetracker.service.StockService;
import com.medicinetracker.util.AuthenticatedUserProvider;
import com.medicinetracker.util.MedicineStatusCalculator;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StockServiceImpl implements StockService {

    private final MedicineRepository medicineRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final StockTransactionMapper stockTransactionMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public StockServiceImpl(MedicineRepository medicineRepository, StockTransactionRepository stockTransactionRepository, StockTransactionMapper stockTransactionMapper, AuthenticatedUserProvider authenticatedUserProvider, AuditService auditService, NotificationService notificationService) {
        this.medicineRepository = medicineRepository;
        this.stockTransactionRepository = stockTransactionRepository;
        this.stockTransactionMapper = stockTransactionMapper;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Override
    public StockTransactionResponse adjustStock(StockAdjustmentRequest request) {
        Medicine medicine = medicineRepository.findByIdAndArchivedFalse(request.medicineId())
                .orElseThrow(() -> new com.medicinetracker.exception.ResourceNotFoundException("Medicine not found"));
        User user = authenticatedUserProvider.getCurrentUser();
        if (user.getBranch() != null && user.getRole() != com.medicinetracker.entity.enums.Role.ADMIN
                && !user.getBranch().getId().equals(medicine.getBranch().getId())) {
            throw new AccessDeniedException("You do not have access to this medicine");
        }

        int quantityBefore = medicine.getQuantity();
        int quantityAfter = quantityBefore + request.quantityChange();
        if (quantityAfter < 0) {
            throw new com.medicinetracker.exception.BadRequestException("Insufficient stock for this adjustment");
        }

        medicine.setQuantity(quantityAfter);
        medicine.setStatus(MedicineStatusCalculator.calculate(medicine));
        if (request.quantityChange() < 0) {
            medicine.setLastSoldAt(OffsetDateTime.now());
        } else if (request.quantityChange() > 0) {
            medicine.setLastRestockedAt(OffsetDateTime.now());
        }
        medicineRepository.save(medicine);

        StockTransaction transaction = new StockTransaction();
        transaction.setMedicine(medicine);
        transaction.setBranch(medicine.getBranch());
        transaction.setPerformedBy(user);
        transaction.setType(request.type());
        transaction.setQuantityBefore(quantityBefore);
        transaction.setQuantityChange(request.quantityChange());
        transaction.setQuantityAfter(quantityAfter);
        transaction.setReferenceNote(request.referenceNote());
        transaction.setUnitPrice(request.unitPrice());
        transaction.setTransactionDate(OffsetDateTime.now());
        StockTransaction saved = stockTransactionRepository.save(transaction);

        if (quantityAfter <= medicine.getReorderLevel()) {
            notificationService.createAndSend(
                    NotificationType.LOW_STOCK_ALERT,
                    "Low stock alert",
                    medicine.getName() + " is running low at branch " + medicine.getBranch().getName(),
                    medicine,
                    user,
                    "EMAIL"
            );
        }
        auditService.record(AuditAction.UPDATE, "STOCK", saved.getId().toString(), "Stock adjusted", "delta=" + request.quantityChange());
        return stockTransactionMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockTransactionResponse> getHistory(UUID medicineId) {
        return stockTransactionRepository.findTop20ByMedicineIdOrderByTransactionDateDesc(medicineId).stream()
                .map(stockTransactionMapper::toResponse)
                .toList();
    }
}

