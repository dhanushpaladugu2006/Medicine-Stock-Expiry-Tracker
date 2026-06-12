package com.medicinetracker.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.medicinetracker.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findByBranchId(UUID branchId);
}
