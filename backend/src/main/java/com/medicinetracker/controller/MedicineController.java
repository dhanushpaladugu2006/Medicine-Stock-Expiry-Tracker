package com.medicinetracker.controller;

import java.time.LocalDate;
import java.util.UUID;

import com.medicinetracker.dto.common.ApiResponse;
import com.medicinetracker.dto.common.PageResponse;
import com.medicinetracker.dto.medicine.BulkUploadResultResponse;
import com.medicinetracker.dto.medicine.MedicineRequest;
import com.medicinetracker.dto.medicine.MedicineResponse;
import com.medicinetracker.service.MedicineService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/medicines")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MedicineResponse>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Medicines loaded", medicineService.searchMedicines(search, category, branchId, stockStatus, expiryFrom, expiryTo, page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicineResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Medicine loaded", medicineService.getMedicine(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MedicineResponse>> create(@Valid @org.springframework.web.bind.annotation.RequestBody MedicineRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Medicine created", medicineService.createMedicine(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicineResponse>> update(@PathVariable UUID id, @Valid @org.springframework.web.bind.annotation.RequestBody MedicineRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Medicine updated", medicineService.updateMedicine(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.ok(ApiResponse.success("Medicine archived", null));
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<ApiResponse<MedicineResponse>> uploadImage(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("Medicine image uploaded", medicineService.uploadMedicineImage(id, file)));
    }

    @PostMapping("/bulk-upload")
    public ResponseEntity<ApiResponse<BulkUploadResultResponse>> bulkUpload(@RequestPart("file") MultipartFile file,
                                                                            @RequestParam(required = false) UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success("Bulk upload completed", medicineService.bulkUpload(file, branchId)));
    }
}

