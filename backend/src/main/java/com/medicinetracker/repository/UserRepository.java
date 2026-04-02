package com.medicinetracker.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.medicinetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findByBranchId(UUID branchId);
}
