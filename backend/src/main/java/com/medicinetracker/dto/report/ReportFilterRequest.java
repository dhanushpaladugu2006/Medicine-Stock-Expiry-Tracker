package com.medicinetracker.dto.report;

import java.time.LocalDate;
import java.util.UUID;

public record ReportFilterRequest(LocalDate fromDate, LocalDate toDate, UUID branchId) {
}
