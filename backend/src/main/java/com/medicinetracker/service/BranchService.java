package com.medicinetracker.service;

import java.util.List;

import com.medicinetracker.dto.branch.BranchRequest;
import com.medicinetracker.dto.branch.BranchResponse;

public interface BranchService {

    BranchResponse createBranch(BranchRequest request);

    List<BranchResponse> getBranches();
}
