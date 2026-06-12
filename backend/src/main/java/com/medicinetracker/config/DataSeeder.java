package com.medicinetracker.config;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.medicinetracker.entity.Branch;
import com.medicinetracker.entity.Medicine;
import com.medicinetracker.entity.User;
import com.medicinetracker.entity.enums.Role;
import com.medicinetracker.repository.BranchRepository;
import com.medicinetracker.repository.MedicineRepository;
import com.medicinetracker.repository.UserRepository;
import com.medicinetracker.util.MedicineStatusCalculator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final MedicineRepository medicineRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(BranchRepository branchRepository, UserRepository userRepository, MedicineRepository medicineRepository, PasswordEncoder passwordEncoder) {
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.medicineRepository = medicineRepository;
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

        if (medicineRepository.count() == 0) {
            seedSampleMedicines(centralBranch);
        }
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

    private void seedSampleMedicines(Branch branch) {
        // Safe stock medicine
        createMedicine("Paracetamol 500mg", "PARA-102-X", "Analgesics", "HealthCorp Ltd", 150, 10,
                new BigDecimal("12.50"), LocalDate.now().plusMonths(6), LocalDate.now().minusMonths(1), "8901030784920", branch);

        // Low stock medicine
        createMedicine("Amoxicillin 250mg", "AMOX-204-Y", "Antibiotics", "PharmaMed Inc", 8, 15,
                new BigDecimal("45.00"), LocalDate.now().plusMonths(3), LocalDate.now().minusMonths(2), "8901030784921", branch);

        // Expired medicine
        createMedicine("Ibuprofen 400mg", "IBU-309-Z", "Analgesics", "HealthCorp Ltd", 80, 10,
                new BigDecimal("22.00"), LocalDate.now().minusDays(5), LocalDate.now().minusMonths(4), "8901030784922", branch);
    }

    private void createMedicine(String name, String batchNumber, String category, String manufacturer, int quantity, int reorderLevel,
                                BigDecimal price, LocalDate expiryDate, LocalDate manufactureDate, String barcode, Branch branch) {
        Medicine medicine = new Medicine();
        medicine.setName(name);
        medicine.setBatchNumber(batchNumber);
        medicine.setCategory(category);
        medicine.setManufacturer(manufacturer);
        medicine.setQuantity(quantity);
        medicine.setReorderLevel(reorderLevel);
        medicine.setPrice(price);
        medicine.setExpiryDate(expiryDate);
        medicine.setManufactureDate(manufactureDate);
        medicine.setBarcode(barcode);
        medicine.setBranch(branch);
        medicine.setArchived(false);
        medicine.setStatus(MedicineStatusCalculator.calculate(medicine));
        medicineRepository.save(medicine);
    }
}
