package com.medicinetracker.service.impl;

import java.time.LocalDate;
import java.util.List;

import com.medicinetracker.entity.Medicine;
import com.medicinetracker.entity.User;
import com.medicinetracker.entity.enums.AuditAction;
import com.medicinetracker.entity.enums.MedicineStatus;
import com.medicinetracker.entity.enums.NotificationType;
import com.medicinetracker.entity.enums.Role;
import com.medicinetracker.repository.MedicineRepository;
import com.medicinetracker.repository.UserRepository;
import com.medicinetracker.service.AuditService;
import com.medicinetracker.service.MedicineService;
import com.medicinetracker.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ExpiryMonitoringScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExpiryMonitoringScheduler.class);

    private final MedicineRepository medicineRepository;
    private final UserRepository userRepository;
    private final MedicineService medicineService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public ExpiryMonitoringScheduler(MedicineRepository medicineRepository, UserRepository userRepository,
            MedicineService medicineService, NotificationService notificationService, AuditService auditService) {
        this.medicineRepository = medicineRepository;
        this.userRepository = userRepository;
        this.medicineService = medicineService;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    @Scheduled(
            cron = "${app.scheduler.expiry-cron}",
            zone = "${app.scheduler.zone-id}")
    @Transactional
    public void runDailyExpiryScan() {
        medicineService.refreshStatuses();
        LocalDate today = LocalDate.now();
        List<Medicine> candidates = medicineRepository.findByArchivedFalseAndExpiryDateLessThanEqual(today.plusDays(30));
        for (Medicine medicine : candidates) {
            if (medicine.getStatus() == MedicineStatus.EXPIRED
                    || medicine.getStatus() == MedicineStatus.NEAR_EXPIRY_30
                    || medicine.getStatus() == MedicineStatus.NEAR_EXPIRY_15
                    || medicine.getStatus() == MedicineStatus.NEAR_EXPIRY_7
                    || medicine.getQuantity() <= medicine.getReorderLevel()) {
                List<User> users = userRepository.findByBranchId(medicine.getBranch().getId()).stream()
                        .filter(User::isActive)
                        .filter(user -> user.getRole() == Role.ADMIN
                                || user.getRole() == Role.PHARMACIST
                                || user.isEmailNotificationsEnabled())
                        .toList();
                for (User user : users) {
                    String message = medicine.getName() + " (batch " + medicine.getBatchNumber() + ") at "
                            + medicine.getBranch().getName() + " is "
                            + medicine.getStatus().name().replace('_', ' ').toLowerCase() + ". Quantity: "
                            + medicine.getQuantity();
                    notificationService.createAndSend(
                            NotificationType.EXPIRY_ALERT,
                            "Medicine status alert",
                            message,
                            medicine,
                            user,
                            "EMAIL");
                }
            }
        }
        auditService.record(
                AuditAction.SCHEDULED_CHECK,
                "SCHEDULER",
                "expiry-monitor",
                "Daily expiry scan completed",
                "count=" + candidates.size());
        log.info("Daily expiry scan completed for {} candidate medicines", candidates.size());
    }
}
