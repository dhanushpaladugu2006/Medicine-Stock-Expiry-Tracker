package com.medicinetracker.repository;

import java.util.List;
import java.util.UUID;

import com.medicinetracker.entity.Notification;
import com.medicinetracker.entity.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findTop20ByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Notification> findByStatus(NotificationStatus status);
}
