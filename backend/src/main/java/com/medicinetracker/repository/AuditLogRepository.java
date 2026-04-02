package com.medicinetracker.repository;

import java.util.List;
import java.util.UUID;

import com.medicinetracker.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findTop50ByOrderByCreatedAtDesc();
}
