package com.medicinetracker.dto.medicine;

import java.util.List;

public record BulkUploadResultResponse(int created, int updated, List<String> errors) {
}
