package com.medicinetracker.service;

import java.util.List;
import java.util.UUID;

import com.medicinetracker.dto.notification.NotificationResponse;
import com.medicinetracker.entity.Medicine;
import com.medicinetracker.entity.User;
import com.medicinetracker.entity.enums.NotificationType;

public interface NotificationService {

    List<NotificationResponse> getCurrentUserNotifications();

    NotificationResponse markAsRead(UUID notificationId);

    void createAndSend(NotificationType type, String title, String message, Medicine medicine, User user, String channel);
}
