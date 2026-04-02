package com.medicinetracker.mapper;

import com.medicinetracker.dto.medicine.MedicineResponse;
import com.medicinetracker.entity.Medicine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MedicineMapper {

    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    @Mapping(target = "lowStock", expression = "java(medicine.getQuantity() <= medicine.getReorderLevel())")
    @Mapping(target = "predictedExpiryRiskScore", ignore = true)
    MedicineResponse toResponse(Medicine medicine);
}
