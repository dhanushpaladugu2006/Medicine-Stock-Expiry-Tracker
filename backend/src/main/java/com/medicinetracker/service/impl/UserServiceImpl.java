package com.medicinetracker.service.impl;

import com.medicinetracker.dto.user.UpdateProfileRequest;
import com.medicinetracker.dto.user.UserProfileResponse;
import com.medicinetracker.entity.User;
import com.medicinetracker.entity.enums.AuditAction;
import com.medicinetracker.mapper.UserMapper;
import com.medicinetracker.repository.UserRepository;
import com.medicinetracker.service.AuditService;
import com.medicinetracker.service.UserService;
import com.medicinetracker.util.AuthenticatedUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuditService auditService;

    public UserServiceImpl(AuthenticatedUserProvider authenticatedUserProvider, UserRepository userRepository, UserMapper userMapper, AuditService auditService) {
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentProfile() {
        return userMapper.toProfile(authenticatedUserProvider.getCurrentUser());
    }

    @Override
    public UserProfileResponse updateCurrentProfile(UpdateProfileRequest request) {
        User user = authenticatedUserProvider.getCurrentUser();
        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone().trim());
        }
        if (request.emailNotificationsEnabled() != null) {
            user.setEmailNotificationsEnabled(request.emailNotificationsEnabled());
        }
        if (request.smsNotificationsEnabled() != null) {
            user.setSmsNotificationsEnabled(request.smsNotificationsEnabled());
        }
        User saved = userRepository.save(user);
        auditService.record(AuditAction.UPDATE, "USER", saved.getId().toString(), "Profile updated", "email=" + saved.getEmail());
        return userMapper.toProfile(saved);
    }
}

