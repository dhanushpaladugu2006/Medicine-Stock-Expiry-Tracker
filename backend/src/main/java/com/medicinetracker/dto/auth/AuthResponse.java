package com.medicinetracker.dto.auth;

import com.medicinetracker.dto.user.UserProfileResponse;

public record AuthResponse(String accessToken, String tokenType, UserProfileResponse user) {
}
