package com.medicinetracker.service.impl;

import java.time.OffsetDateTime;
import java.util.Map;

import com.medicinetracker.dto.auth.AuthResponse;
import com.medicinetracker.dto.auth.LoginRequest;
import com.medicinetracker.dto.auth.RegisterRequest;
import com.medicinetracker.entity.Branch;
import com.medicinetracker.entity.User;
import com.medicinetracker.entity.enums.AuditAction;
import com.medicinetracker.entity.enums.Role;
import com.medicinetracker.exception.BadRequestException;
import com.medicinetracker.exception.ConflictException;
import com.medicinetracker.exception.ResourceNotFoundException;
import com.medicinetracker.mapper.UserMapper;
import com.medicinetracker.repository.BranchRepository;
import com.medicinetracker.repository.UserRepository;
import com.medicinetracker.security.JwtService;
import com.medicinetracker.service.AuditService;
import com.medicinetracker.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final AuditService auditService;

    public AuthServiceImpl(UserRepository userRepository, BranchRepository branchRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService, UserMapper userMapper, AuditService auditService) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.auditService = auditService;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmailIgnoreCase(request.email()).isPresent()) {
            throw new ConflictException("An account with this email already exists");
        }
        if (request.role() != Role.ADMIN && request.branchId() == null) {
            throw new BadRequestException("Branch selection is required for pharmacist and staff accounts");
        }

        Branch branch = resolveBranch(request.branchId());
        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setRole(request.role());
        user.setBranch(branch);
        user.setLastLoginAt(OffsetDateTime.now());

        User savedUser = userRepository.save(user);
        auditService.record(AuditAction.CREATE, "USER", savedUser.getId().toString(), "User account created", "role=" + savedUser.getRole());
        return buildAuthResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid credentials"));
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);
        auditService.record(AuditAction.LOGIN, "USER", user.getId().toString(), "User logged in", "email=" + user.getEmail());
        return buildAuthResponse(user);
    }

    private Branch resolveBranch(java.util.UUID branchId) {
        if (branchId == null) {
            return null;
        }
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user, Map.of(
                "role", user.getRole().name(),
                "fullName", user.getFullName(),
                "branchId", user.getBranch() != null ? user.getBranch().getId().toString() : ""
        ));
        return new AuthResponse(token, "Bearer", userMapper.toProfile(user));
    }
}

