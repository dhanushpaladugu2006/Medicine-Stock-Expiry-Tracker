package com.medicinetracker.dto.user;

import java.util.UUID;

import com.medicinetracker.entity.enums.Role;

public record UserProfileResponse(
        UUID id,
        String fullName,
        String email,
        String phone,
        Role role,
        UUID branchId,
        String branchName,
        boolean emailNotificationsEnabled,
        boolean smsNotificationsEnabled
) {
}
