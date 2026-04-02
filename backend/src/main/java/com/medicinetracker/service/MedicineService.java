package com.medicinetracker.service;

import java.time.LocalDate;
import java.util.UUID;

import com.medicinetracker.dto.common.PageResponse;
import com.medicinetracker.dto.medicine.BulkUploadResultResponse;
import com.medicinetracker.dto.medicine.MedicineRequest;
import com.medicinetracker.dto.medicine.MedicineResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MedicineService {

    MedicineResponse createMedicine(MedicineRequest request);

    MedicineResponse updateMedicine(UUID id, MedicineRequest request);

    void deleteMedicine(UUID id);

    MedicineResponse getMedicine(UUID id);

    PageResponse<MedicineResponse> searchMedicines(String search, String category, UUID branchId, String stockStatus,
                                                   LocalDate expiryFrom, LocalDate expiryTo, int page, int size);

    MedicineResponse uploadMedicineImage(UUID id, MultipartFile file);

    BulkUploadResultResponse bulkUpload(MultipartFile file, UUID branchId);

    void refreshStatuses();
}
