package com.medicinetracker.dto.common;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.medicinetracker.entity.enums.AuditAction;

public record AuditLogResponse(
        UUID id,
        AuditAction action,
        String entityType,
        String entityId,
        String actorEmail,
        String description,
        String metadata,
        OffsetDateTime createdAt
) {
}
