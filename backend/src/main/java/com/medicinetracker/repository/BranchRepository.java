package com.medicinetracker.repository;

import java.util.Optional;
import java.util.UUID;

import com.medicinetracker.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, UUID> {

    Optional<Branch> findByCodeIgnoreCase(String code);
}
