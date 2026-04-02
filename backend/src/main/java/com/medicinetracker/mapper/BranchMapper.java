package com.medicinetracker.mapper;

import com.medicinetracker.dto.branch.BranchResponse;
import com.medicinetracker.entity.Branch;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BranchMapper {

    BranchResponse toResponse(Branch branch);
}
