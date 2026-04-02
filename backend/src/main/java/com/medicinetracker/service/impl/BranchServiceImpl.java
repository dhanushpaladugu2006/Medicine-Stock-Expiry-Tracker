package com.medicinetracker.service.impl;

import java.util.Comparator;
import java.util.List;

import com.medicinetracker.dto.branch.BranchRequest;
import com.medicinetracker.dto.branch.BranchResponse;
import com.medicinetracker.entity.Branch;
import com.medicinetracker.entity.enums.AuditAction;
import com.medicinetracker.exception.ConflictException;
import com.medicinetracker.mapper.BranchMapper;
import com.medicinetracker.repository.BranchRepository;
import com.medicinetracker.service.AuditService;
import com.medicinetracker.service.BranchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;
    private final AuditService auditService;

    public BranchServiceImpl(BranchRepository branchRepository, BranchMapper branchMapper, AuditService auditService) {
        this.branchRepository = branchRepository;
        this.branchMapper = branchMapper;
        this.auditService = auditService;
    }

    @Override
    public BranchResponse createBranch(BranchRequest request) {
        branchRepository.findByCodeIgnoreCase(request.code()).ifPresent(existing -> {
            throw new ConflictException("Branch code already exists");
        });

        Branch branch = new Branch();
        branch.setName(request.name());
        branch.setCode(request.code().trim().toUpperCase());
        branch.setAddress(request.address());
        branch.setCity(request.city());
        branch.setState(request.state());
        branch.setCountry(request.country());
        branch.setPhone(request.phone());
        branch.setEmail(request.email());
        branch.setActive(request.active() == null || request.active());

        Branch saved = branchRepository.save(branch);
        auditService.record(AuditAction.CREATE, "BRANCH", saved.getId().toString(), "Branch created", "code=" + saved.getCode());
        return branchMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchResponse> getBranches() {
        return branchRepository.findAll().stream()
                .sorted(Comparator.comparing(Branch::getName, String.CASE_INSENSITIVE_ORDER))
                .map(branchMapper::toResponse)
                .toList();
    }
}

