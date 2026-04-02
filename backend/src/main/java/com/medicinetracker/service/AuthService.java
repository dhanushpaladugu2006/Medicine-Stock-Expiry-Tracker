package com.medicinetracker.service;

import com.medicinetracker.dto.auth.AuthResponse;
import com.medicinetracker.dto.auth.LoginRequest;
import com.medicinetracker.dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
