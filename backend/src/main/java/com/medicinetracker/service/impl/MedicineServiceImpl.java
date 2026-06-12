package com.medicinetracker.service.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import com.medicinetracker.config.AppProperties;
import com.medicinetracker.dto.common.PageResponse;
import com.medicinetracker.dto.medicine.BulkUploadResultResponse;
import com.medicinetracker.dto.medicine.MedicineRequest;
import com.medicinetracker.dto.medicine.MedicineResponse;
import com.medicinetracker.entity.Branch;
import com.medicinetracker.entity.Medicine;
import com.medicinetracker.entity.StockTransaction;
import com.medicinetracker.entity.User;
import com.medicinetracker.entity.enums.AuditAction;
import com.medicinetracker.entity.enums.Role;
import com.medicinetracker.entity.enums.StockTransactionType;
import com.medicinetracker.exception.BadRequestException;
import com.medicinetracker.exception.ConflictException;
import com.medicinetracker.exception.ResourceNotFoundException;
import com.medicinetracker.mapper.MedicineMapper;
import com.medicinetracker.repository.BranchRepository;
import com.medicinetracker.repository.MedicineRepository;
import com.medicinetracker.repository.StockTransactionRepository;
import com.medicinetracker.service.AuditService;
import com.medicinetracker.service.MedicineService;
import com.medicinetracker.util.AuthenticatedUserProvider;
import com.medicinetracker.util.FileValidationUtils;
import com.medicinetracker.util.MedicineStatusCalculator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bson.Document;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;
    private final BranchRepository branchRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final MedicineMapper medicineMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final AuditService auditService;
    private final AppProperties appProperties;
    private final MongoTemplate mongoTemplate;

    public MedicineServiceImpl(MedicineRepository medicineRepository, BranchRepository branchRepository, StockTransactionRepository stockTransactionRepository, MedicineMapper medicineMapper, AuthenticatedUserProvider authenticatedUserProvider, AuditService auditService, AppProperties appProperties, MongoTemplate mongoTemplate) {
        this.medicineRepository = medicineRepository;
        this.branchRepository = branchRepository;
        this.stockTransactionRepository = stockTransactionRepository;
        this.medicineMapper = medicineMapper;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.auditService = auditService;
        this.appProperties = appProperties;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public MedicineResponse createMedicine(MedicineRequest request) {
        validateDates(request);
        Branch branch = resolveWritableBranch(request.branchId());
        medicineRepository.findByBranchIdAndBatchNumberIgnoreCase(branch.getId(), request.batchNumber())
                .ifPresent(existing -> {
                    throw new ConflictException("Batch number already exists for the selected branch");
                });

        Medicine medicine = new Medicine();
        applyRequest(medicine, request, branch);
        Medicine saved = medicineRepository.save(medicine);
        createStockLog(saved, 0, saved.getQuantity(), StockTransactionType.PURCHASE, "Initial stock", request.price());
        auditService.record(AuditAction.CREATE, "MEDICINE", saved.getId().toString(), "Medicine created", "batch=" + saved.getBatchNumber());
        return toResponse(saved);
    }

    @Override
    public MedicineResponse updateMedicine(UUID id, MedicineRequest request) {
        validateDates(request);
        Medicine medicine = medicineRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found"));
        enforceBranchAccess(medicine.getBranch().getId());

        UUID targetBranchId = request.branchId() != null ? request.branchId() : medicine.getBranch().getId();
        Branch branch = resolveWritableBranch(targetBranchId);
        medicineRepository.findByBranchIdAndBatchNumberIgnoreCase(branch.getId(), request.batchNumber())
                .filter(existing -> !existing.getId().equals(medicine.getId()))
                .ifPresent(existing -> {
                    throw new ConflictException("Batch number already exists for the selected branch");
                });

        int previousQuantity = medicine.getQuantity();
        applyRequest(medicine, request, branch);
        Medicine saved = medicineRepository.save(medicine);
        if (previousQuantity != saved.getQuantity()) {
            int delta = saved.getQuantity() - previousQuantity;
            createStockLog(saved, previousQuantity, delta, StockTransactionType.ADJUSTMENT, "Quantity updated from medicine editor", request.price());
        }
        auditService.record(AuditAction.UPDATE, "MEDICINE", saved.getId().toString(), "Medicine updated", "batch=" + saved.getBatchNumber());
        return toResponse(saved);
    }

    @Override
    public void deleteMedicine(UUID id) {
        Medicine medicine = medicineRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found"));
        enforceBranchAccess(medicine.getBranch().getId());
        medicine.setArchived(true);
        medicineRepository.save(medicine);
        auditService.record(AuditAction.DELETE, "MEDICINE", medicine.getId().toString(), "Medicine archived", "batch=" + medicine.getBatchNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public MedicineResponse getMedicine(UUID id) {
        Medicine medicine = medicineRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found"));
        enforceBranchAccess(medicine.getBranch().getId());
        return toResponse(medicine);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MedicineResponse> searchMedicines(String search, String category, UUID branchId, String stockStatus,
                                                          LocalDate expiryFrom, LocalDate expiryTo, int page, int size) {
        UUID effectiveBranchId = resolveReadableBranch(branchId);
        Query query = new Query();
        query.addCriteria(Criteria.where("archived").is(false));

        if (effectiveBranchId != null) {
            query.addCriteria(Criteria.where("branch").is(effectiveBranchId));
        }

        if (search != null && !search.isBlank()) {
            String regex = ".*" + Pattern.quote(search.trim()) + ".*";
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("name").regex(regex, "i"),
                    Criteria.where("batchNumber").regex(regex, "i"),
                    Criteria.where("category").regex(regex, "i")
            ));
        }

        if (category != null && !category.isBlank()) {
            query.addCriteria(Criteria.where("category").regex("^" + Pattern.quote(category.trim()) + "$", "i"));
        }

        if (stockStatus != null && !stockStatus.isBlank()) {
            switch (stockStatus.trim().toLowerCase()) {
                case "low":
                    query.addCriteria(Criteria.where("$expr").is(new Document("$lte", Arrays.asList("$quantity", "$reorderLevel"))));
                    break;
                case "out":
                    query.addCriteria(Criteria.where("quantity").lte(0));
                    break;
                case "healthy":
                    query.addCriteria(Criteria.where("$expr").is(new Document("$gt", Arrays.asList("$quantity", "$reorderLevel"))));
                    break;
            }
        }

        if (expiryFrom != null && expiryTo != null) {
            query.addCriteria(Criteria.where("expiryDate").gte(expiryFrom).lte(expiryTo));
        } else if (expiryFrom != null) {
            query.addCriteria(Criteria.where("expiryDate").gte(expiryFrom));
        } else if (expiryTo != null) {
            query.addCriteria(Criteria.where("expiryDate").lte(expiryTo));
        }

        long total = mongoTemplate.count(query, Medicine.class);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "expiryDate", "name"));
        query.with(pageable);
        List<Medicine> list = mongoTemplate.find(query, Medicine.class);
        List<MedicineResponse> content = list.stream().map(this::toResponse).toList();
        long totalPages = (long) Math.ceil((double) total / size);
        return new PageResponse<>(content, total, (int) totalPages, page, size);
    }

    @Override
    public MedicineResponse uploadMedicineImage(UUID id, MultipartFile file) {
        FileValidationUtils.validateNotEmpty(file, "Image file is required");
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }

        Medicine medicine = medicineRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found"));
        enforceBranchAccess(medicine.getBranch().getId());

        try {
            Path uploadDir = Path.of(appProperties.storage().uploadDir(), "medicine-images");
            Files.createDirectories(uploadDir);
            String original = file.getOriginalFilename() == null ? "image.png" : file.getOriginalFilename();
            String extension = original.contains(".") ? original.substring(original.lastIndexOf('.')) : ".png";
            String filename = medicine.getId() + "-" + System.currentTimeMillis() + extension;
            Path target = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            medicine.setImageUrl("/uploads/medicine-images/" + filename);
            Medicine saved = medicineRepository.save(medicine);
            auditService.record(AuditAction.UPDATE, "MEDICINE", saved.getId().toString(), "Medicine image uploaded", filename);
            return toResponse(saved);
        } catch (IOException exception) {
            throw new BadRequestException("Failed to upload image: " + exception.getMessage());
        }
    }

    @Override
    public BulkUploadResultResponse bulkUpload(MultipartFile file, UUID branchId) {
        FileValidationUtils.validateNotEmpty(file, "CSV file is required");
        String contentType = file.getContentType();
        if (contentType != null && !(contentType.equals("text/csv") || contentType.equals("application/vnd.ms-excel"))) {
            throw new BadRequestException("Only CSV files are supported");
        }

        Branch branch = resolveWritableBranch(branchId);
        int created = 0;
        int updated = 0;
        List<String> errors = new ArrayList<>();

        try (CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build()
                .parse(new InputStreamReader(file.getInputStream()))) {
            for (CSVRecord record : parser) {
                try {
                    MedicineRequest request = new MedicineRequest(
                            record.get("name"),
                            record.get("batchNumber"),
                            record.get("category"),
                            record.get("manufacturer"),
                            Integer.parseInt(record.get("quantity")),
                            Integer.parseInt(record.isMapped("reorderLevel") ? record.get("reorderLevel") : "10"),
                            new java.math.BigDecimal(record.get("price")),
                            LocalDate.parse(record.get("expiryDate")),
                            LocalDate.parse(record.get("manufactureDate")),
                            record.isMapped("barcode") ? record.get("barcode") : null,
                            branch.getId()
                    );
                    validateDates(request);
                    Medicine existing = medicineRepository.findByBranchIdAndBatchNumberIgnoreCase(branch.getId(), request.batchNumber()).orElse(null);
                    if (existing == null) {
                        Medicine medicine = new Medicine();
                        applyRequest(medicine, request, branch);
                        Medicine saved = medicineRepository.save(medicine);
                        createStockLog(saved, 0, saved.getQuantity(), StockTransactionType.BULK_UPLOAD, "CSV bulk upload", request.price());
                        created++;
                    } else {
                        int previous = existing.getQuantity();
                        applyRequest(existing, request, branch);
                        Medicine saved = medicineRepository.save(existing);
                        int delta = saved.getQuantity() - previous;
                        if (delta != 0) {
                            createStockLog(saved, previous, delta, StockTransactionType.BULK_UPLOAD, "CSV bulk upload update", request.price());
                        }
                        updated++;
                    }
                } catch (Exception exception) {
                    errors.add("Row " + record.getRecordNumber() + ": " + exception.getMessage());
                }
            }
        } catch (IOException exception) {
            throw new BadRequestException("Unable to parse CSV file");
        }

        auditService.record(AuditAction.CREATE, "MEDICINE_BULK_UPLOAD", branch.getId().toString(), "Medicine bulk upload completed", "created=" + created + ",updated=" + updated);
        return new BulkUploadResultResponse(created, updated, errors);
    }

    @Override
    public void refreshStatuses() {
        Query query = new Query();
        query.addCriteria(Criteria.where("archived").is(false));
        mongoTemplate.find(query, Medicine.class).forEach(medicine -> {
            medicine.setStatus(MedicineStatusCalculator.calculate(medicine));
            medicineRepository.save(medicine);
        });
    }

    private void applyRequest(Medicine medicine, MedicineRequest request, Branch branch) {
        medicine.setName(request.name().trim());
        medicine.setBatchNumber(request.batchNumber().trim().toUpperCase());
        medicine.setCategory(request.category().trim());
        medicine.setManufacturer(request.manufacturer().trim());
        medicine.setQuantity(request.quantity());
        medicine.setReorderLevel(request.reorderLevel());
        medicine.setPrice(request.price());
        medicine.setExpiryDate(request.expiryDate());
        medicine.setManufactureDate(request.manufactureDate());
        medicine.setBarcode(request.barcode());
        medicine.setBranch(branch);
        medicine.setStatus(MedicineStatusCalculator.calculate(medicine));
        if (request.quantity() > 0) {
            medicine.setLastRestockedAt(OffsetDateTime.now());
        }
    }

    private void validateDates(MedicineRequest request) {
        if (request.manufactureDate().isAfter(request.expiryDate())) {
            throw new BadRequestException("Manufacture date cannot be after expiry date");
        }
    }

    private Branch resolveWritableBranch(UUID branchId) {
        User user = authenticatedUserProvider.getCurrentUser();
        if (user.getRole() == Role.ADMIN) {
            if (branchId == null) {
                throw new BadRequestException("Branch is required");
            }
            return branchRepository.findById(branchId)
                    .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        }
        if (user.getBranch() == null) {
            throw new BadRequestException("Your account is not mapped to a branch");
        }
        if (branchId != null && !user.getBranch().getId().equals(branchId)) {
            throw new AccessDeniedException("You can only manage medicines for your branch");
        }
        return user.getBranch();
    }

    private UUID resolveReadableBranch(UUID requestedBranchId) {
        User user = authenticatedUserProvider.getCurrentUser();
        if (user.getRole() == Role.ADMIN) {
            return requestedBranchId;
        }
        if (user.getBranch() == null) {
            throw new BadRequestException("Your account is not mapped to a branch");
        }
        if (requestedBranchId != null && !user.getBranch().getId().equals(requestedBranchId)) {
            throw new AccessDeniedException("You can only view medicines for your branch");
        }
        return user.getBranch().getId();
    }

    private void enforceBranchAccess(UUID branchId) {
        User user = authenticatedUserProvider.getCurrentUser();
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (user.getBranch() == null || !user.getBranch().getId().equals(branchId)) {
            throw new AccessDeniedException("You do not have access to this branch data");
        }
    }

    private void createStockLog(Medicine medicine, int quantityBefore, int quantityChange, StockTransactionType type,
                                String note, java.math.BigDecimal unitPrice) {
        StockTransaction transaction = new StockTransaction();
        transaction.setMedicine(medicine);
        transaction.setBranch(medicine.getBranch());
        transaction.setPerformedBy(authenticatedUserProvider.getCurrentUser());
        transaction.setType(type);
        transaction.setQuantityBefore(quantityBefore);
        transaction.setQuantityChange(quantityChange);
        transaction.setQuantityAfter(quantityBefore + quantityChange);
        transaction.setReferenceNote(note);
        transaction.setUnitPrice(unitPrice);
        transaction.setTransactionDate(OffsetDateTime.now());
        stockTransactionRepository.save(transaction);
    }

    private MedicineResponse toResponse(Medicine medicine) {
        long riskScore = calculateRiskScore(medicine);
        MedicineResponse response = medicineMapper.toResponse(medicine);
        return new MedicineResponse(
                response.id(),
                response.name(),
                response.batchNumber(),
                response.category(),
                response.manufacturer(),
                response.quantity(),
                response.reorderLevel(),
                response.price(),
                response.expiryDate(),
                response.manufactureDate(),
                response.barcode(),
                response.imageUrl(),
                response.status(),
                response.branchId(),
                response.branchName(),
                response.lowStock(),
                riskScore
        );
    }

    private long calculateRiskScore(Medicine medicine) {
        long daysToExpiry = Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), medicine.getExpiryDate()));
        List<StockTransaction> txs = stockTransactionRepository.findByMedicineIdAndTransactionDateGreaterThanEqual(medicine.getId(), OffsetDateTime.now().minusDays(30));
        int monthlyConsumption = txs.stream().filter(t -> t.getQuantityChange() < 0).mapToInt(t -> Math.abs(t.getQuantityChange())).sum();
        long demandPressure = monthlyConsumption == 0 ? 15 : Math.min(80, (long) monthlyConsumption * 2);
        long expiryPressure = daysToExpiry > 60 ? 10 : Math.max(10, 100 - daysToExpiry);
        long quantityPressure = medicine.getQuantity() > medicine.getReorderLevel() * 2L ? 20 : 60;
        return Math.min(100, (demandPressure + expiryPressure + quantityPressure) / 3);
    }
}

