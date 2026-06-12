package com.medicinetracker.repository;

import java.util.Optional;
import java.util.UUID;

import com.medicinetracker.entity.Branch;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BranchRepository extends MongoRepository<Branch, UUID> {

    Optional<Branch> findByCodeIgnoreCase(String code);
}
