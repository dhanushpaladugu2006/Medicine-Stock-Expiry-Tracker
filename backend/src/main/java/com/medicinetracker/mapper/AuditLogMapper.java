package com.medicinetracker.mapper;

import com.medicinetracker.dto.common.AuditLogResponse;
import com.medicinetracker.entity.AuditLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    AuditLogResponse toResponse(AuditLog auditLog);
}
