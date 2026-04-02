package com.medicinetracker.service;

import com.medicinetracker.dto.user.UpdateProfileRequest;
import com.medicinetracker.dto.user.UserProfileResponse;

public interface UserService {

    UserProfileResponse getCurrentProfile();

    UserProfileResponse updateCurrentProfile(UpdateProfileRequest request);
}
