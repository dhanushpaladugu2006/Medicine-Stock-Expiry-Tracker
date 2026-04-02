package com.medicinetracker.dto.branch;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record BranchRequest(
        @NotBlank String name,
        @NotBlank String code,
        @NotBlank String address,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank String country,
        String phone,
        @Email String email,
        Boolean active
) {
}
