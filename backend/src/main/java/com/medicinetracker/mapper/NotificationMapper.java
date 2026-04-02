package com.medicinetracker.mapper;

import com.medicinetracker.dto.notification.NotificationResponse;
import com.medicinetracker.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
}
