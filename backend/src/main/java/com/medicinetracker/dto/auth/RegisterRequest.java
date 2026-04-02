package com.medicinetracker.dto.auth;

import java.util.UUID;

import com.medicinetracker.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String fullName,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 120) String password,
        String phone,
        @NotNull Role role,
        UUID branchId
) {
}
