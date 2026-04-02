package com.medicinetracker.service.impl;

import java.util.List;
import java.util.Optional;

import com.medicinetracker.dto.common.AuditLogResponse;
import com.medicinetracker.entity.AuditLog;
import com.medicinetracker.entity.enums.AuditAction;
import com.medicinetracker.repository.AuditLogRepository;
import com.medicinetracker.service.AuditService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void record(AuditAction action, String entityType, String entityId, String description, String metadata) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String actor = Optional.ofNullable(authentication)
                .map(Authentication::getName)
                .filter(name -> !name.isBlank())
                .orElse("system");

        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setActorEmail(actor);
        auditLog.setDescription(description);
        auditLog.setMetadata(metadata);
        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> recentLogs() {
        return auditLogRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getActorEmail(),
                auditLog.getDescription(),
                auditLog.getMetadata(),
                auditLog.getCreatedAt()
        );
    }
}

