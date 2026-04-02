package com.medicinetracker.service;

import com.medicinetracker.dto.report.ReportFilterRequest;

public interface ReportService {

    byte[] exportReport(String type, String format, ReportFilterRequest filter);
}
