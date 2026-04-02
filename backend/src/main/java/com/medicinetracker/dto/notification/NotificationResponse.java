package com.medicinetracker.dto.notification;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.medicinetracker.entity.enums.NotificationStatus;
import com.medicinetracker.entity.enums.NotificationType;

public record NotificationResponse(
        UUID id,
        String title,
        String message,
        NotificationType type,
        NotificationStatus status,
        String channel,
        OffsetDateTime createdAt,
        OffsetDateTime readAt
) {
}
