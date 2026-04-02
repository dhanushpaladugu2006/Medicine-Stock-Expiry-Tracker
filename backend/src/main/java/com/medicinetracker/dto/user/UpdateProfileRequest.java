package com.medicinetracker.dto.user;

public record UpdateProfileRequest(
        String fullName,
        String phone,
        Boolean emailNotificationsEnabled,
        Boolean smsNotificationsEnabled
) {
}
