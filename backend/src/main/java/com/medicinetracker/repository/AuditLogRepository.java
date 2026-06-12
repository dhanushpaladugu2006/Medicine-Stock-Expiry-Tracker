package com.medicinetracker.repository;

import java.util.List;
import java.util.UUID;

import com.medicinetracker.entity.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditLogRepository extends MongoRepository<AuditLog, UUID> {

    List<AuditLog> findTop50ByOrderByCreatedAtDesc();
}
