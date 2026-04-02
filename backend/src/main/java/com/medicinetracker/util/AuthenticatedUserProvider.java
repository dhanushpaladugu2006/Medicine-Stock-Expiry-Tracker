package com.medicinetracker.util;

import java.util.UUID;

import com.medicinetracker.entity.User;
import com.medicinetracker.exception.UnauthorizedException;
import com.medicinetracker.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserProvider {

    private final UserRepository userRepository;

    public AuthenticatedUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
    }

    public UUID getCurrentBranchIdOrNull() {
        User user = getCurrentUser();
        return user.getBranch() != null ? user.getBranch().getId() : null;
    }
}

