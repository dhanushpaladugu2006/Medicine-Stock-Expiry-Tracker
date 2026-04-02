package com.medicinetracker.controller;

import java.util.List;

import com.medicinetracker.dto.common.ApiResponse;
import com.medicinetracker.dto.common.AuditLogResponse;
import com.medicinetracker.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> recentLogs() {
        return ResponseEntity.ok(ApiResponse.success("Audit logs loaded", auditService.recentLogs()));
    }
}

