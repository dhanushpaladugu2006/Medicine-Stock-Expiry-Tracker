package com.medicinetracker.config;

import com.medicinetracker.entity.Branch;
import com.medicinetracker.entity.User;
import com.medicinetracker.entity.enums.Role;
import com.medicinetracker.repository.BranchRepository;
import com.medicinetracker.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(BranchRepository branchRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Branch centralBranch = branchRepository.findByCodeIgnoreCase("CENTRAL")
                .orElseGet(this::createCentralBranch);

        seedOrUpdateUser("admin@medicinetracker.com", "System Admin", "9999999999", Role.ADMIN, null, "Admin@123");
        seedOrUpdateUser("pharmacist@medicinetracker.com", "Main Pharmacist", "8888888888", Role.PHARMACIST, centralBranch, "Pharma@123");
        seedOrUpdateUser("staff@medicinetracker.com", "Store Staff", "7777777777", Role.STAFF, centralBranch, "Staff@123");
    }

    private Branch createCentralBranch() {
        Branch branch = new Branch();
        branch.setName("Central Pharmacy");
        branch.setCode("CENTRAL");
        branch.setAddress("12 Health Street");
        branch.setCity("Mumbai");
        branch.setState("Maharashtra");
        branch.setCountry("India");
        branch.setPhone("+91-9000000000");
        branch.setEmail("central@medicinetracker.local");
        branch.setActive(true);
        return branchRepository.save(branch);
    }

    private void seedOrUpdateUser(String email, String fullName, String phone, Role role, Branch branch, String rawPassword) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseGet(User::new);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);
        user.setBranch(branch);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setActive(true);
        user.setEmailNotificationsEnabled(true);
        user.setSmsNotificationsEnabled(false);
        userRepository.save(user);
    }
}

