package com.medicinetracker.mapper;

import com.medicinetracker.dto.stock.StockTransactionResponse;
import com.medicinetracker.entity.StockTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockTransactionMapper {

    @Mapping(target = "medicineId", source = "medicine.id")
    @Mapping(target = "medicineName", source = "medicine.name")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    @Mapping(target = "performedBy", source = "performedBy.fullName")
    StockTransactionResponse toResponse(StockTransaction transaction);
}
