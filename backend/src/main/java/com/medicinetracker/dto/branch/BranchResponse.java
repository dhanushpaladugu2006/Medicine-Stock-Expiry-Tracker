package com.medicinetracker.dto.branch;

import java.util.UUID;

public record BranchResponse(
        UUID id,
        String name,
        String code,
        String address,
        String city,
        String state,
        String country,
        String phone,
        String email,
        boolean active
) {
}
