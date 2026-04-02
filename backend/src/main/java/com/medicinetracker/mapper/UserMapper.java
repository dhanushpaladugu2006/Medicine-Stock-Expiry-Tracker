package com.medicinetracker.mapper;

import com.medicinetracker.dto.user.UserProfileResponse;
import com.medicinetracker.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    UserProfileResponse toProfile(User user);
}
