package com.medicinetracker.util;

import java.time.LocalDate;
import java.util.UUID;

import com.medicinetracker.entity.Medicine;
import org.springframework.data.jpa.domain.Specification;

public final class MedicineSpecifications {

    private MedicineSpecifications() {
    }

    public static Specification<Medicine> filter(String search, String category, UUID branchId, String stockStatus,
                                                 LocalDate expiryFrom, LocalDate expiryTo) {
        return Specification.where(notArchived())
                .and(matchesSearch(search))
                .and(matchesCategory(category))
                .and(matchesBranch(branchId))
                .and(matchesStock(stockStatus))
                .and(expiryAfter(expiryFrom))
                .and(expiryBefore(expiryTo));
    }

    private static Specification<Medicine> notArchived() {
        return (root, query, cb) -> cb.isFalse(root.get("archived"));
    }

    private static Specification<Medicine> matchesSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("batchNumber")), pattern),
                    cb.like(cb.lower(root.get("category")), pattern)
            );
        };
    }

    private static Specification<Medicine> matchesCategory(String category) {
        return (root, query, cb) -> category == null || category.isBlank()
                ? cb.conjunction()
                : cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase());
    }

    private static Specification<Medicine> matchesBranch(UUID branchId) {
        return (root, query, cb) -> branchId == null ? cb.conjunction() : cb.equal(root.get("branch").get("id"), branchId);
    }

    private static Specification<Medicine> matchesStock(String stockStatus) {
        return (root, query, cb) -> {
            if (stockStatus == null || stockStatus.isBlank()) {
                return cb.conjunction();
            }
            return switch (stockStatus.trim().toLowerCase()) {
                case "low" -> cb.lessThanOrEqualTo(root.get("quantity"), root.get("reorderLevel"));
                case "out" -> cb.lessThanOrEqualTo(root.get("quantity"), 0);
                case "healthy" -> cb.greaterThan(root.get("quantity"), root.get("reorderLevel"));
                default -> cb.conjunction();
            };
        };
    }

    private static Specification<Medicine> expiryAfter(LocalDate expiryFrom) {
        return (root, query, cb) -> expiryFrom == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("expiryDate"), expiryFrom);
    }

    private static Specification<Medicine> expiryBefore(LocalDate expiryTo) {
        return (root, query, cb) -> expiryTo == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("expiryDate"), expiryTo);
    }
}
