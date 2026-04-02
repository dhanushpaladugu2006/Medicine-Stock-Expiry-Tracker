package com.medicinetracker.service;

import java.util.List;

import com.medicinetracker.dto.common.AuditLogResponse;
import com.medicinetracker.entity.enums.AuditAction;

public interface AuditService {

    void record(AuditAction action, String entityType, String entityId, String description, String metadata);

    List<AuditLogResponse> recentLogs();
}
