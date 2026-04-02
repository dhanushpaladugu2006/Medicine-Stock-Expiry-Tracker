package com.medicinetracker.service.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.medicinetracker.config.AppProperties;
import com.medicinetracker.dto.notification.NotificationResponse;
import com.medicinetracker.entity.Medicine;
import com.medicinetracker.entity.Notification;
import com.medicinetracker.entity.User;
import com.medicinetracker.entity.enums.NotificationStatus;
import com.medicinetracker.entity.enums.NotificationType;
import com.medicinetracker.exception.ResourceNotFoundException;
import com.medicinetracker.mapper.NotificationMapper;
import com.medicinetracker.repository.NotificationRepository;
import com.medicinetracker.service.NotificationService;
import com.medicinetracker.util.AuthenticatedUserProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final JavaMailSender javaMailSender;
    private final AppProperties appProperties;

    public NotificationServiceImpl(NotificationRepository notificationRepository, NotificationMapper notificationMapper, AuthenticatedUserProvider authenticatedUserProvider, JavaMailSender javaMailSender, AppProperties appProperties) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.javaMailSender = javaMailSender;
        this.appProperties = appProperties;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getCurrentUserNotifications() {
        User user = authenticatedUserProvider.getCurrentUser();
        return notificationRepository.findTop20ByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    public NotificationResponse markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(OffsetDateTime.now());
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Override
    public void createAndSend(NotificationType type, String title, String message, Medicine medicine, User user, String channel) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setMedicine(medicine);
        notification.setUser(user);
        notification.setBranch(medicine != null ? medicine.getBranch() : (user != null ? user.getBranch() : null));
        notification.setChannel(channel == null ? "IN_APP" : channel);
        notification.setRecipient(user != null ? user.getEmail() : null);
        notification.setStatus(NotificationStatus.PENDING);
        notification = notificationRepository.save(notification);

        boolean canEmail = "EMAIL".equalsIgnoreCase(notification.getChannel())
                && user != null
                && user.isEmailNotificationsEnabled()
                && appProperties.notification().enableEmail()
                && notification.getRecipient() != null;

        if (canEmail) {
            try {
                SimpleMailMessage mail = new SimpleMailMessage();
                mail.setTo(notification.getRecipient());
                mail.setSubject(title);
                mail.setText(message);
                mail.setFrom(appProperties.notification().senderEmail());
                javaMailSender.send(mail);
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(OffsetDateTime.now());
            } catch (Exception exception) {
                notification.setStatus(NotificationStatus.FAILED);
            }
        } else {
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(OffsetDateTime.now());
        }
        notificationRepository.save(notification);
    }
}

