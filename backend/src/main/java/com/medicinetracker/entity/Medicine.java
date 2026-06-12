package com.medicinetracker.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.medicinetracker.entity.enums.MedicineStatus;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Document(collection = "medicines")
@CompoundIndex(name = "uk_medicine_branch_batch", def = "{'branch': 1, 'batchNumber': 1}", unique = true)
public class Medicine extends BaseEntity {

    private String name;

    private String batchNumber;

    private String category;

    private String manufacturer;

    private Integer quantity;

    private Integer reorderLevel = 10;

    private BigDecimal price;

    private LocalDate expiryDate;

    private LocalDate manufactureDate;

    private String barcode;

    private String imageUrl;

    private MedicineStatus status = MedicineStatus.SAFE;

    @DocumentReference
    private Branch branch;

    private boolean archived = false;

    private OffsetDateTime lastSoldAt;

    private OffsetDateTime lastRestockedAt;

    public Medicine() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(Integer reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public LocalDate getManufactureDate() {
        return manufactureDate;
    }

    public void setManufactureDate(LocalDate manufactureDate) {
        this.manufactureDate = manufactureDate;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public MedicineStatus getStatus() {
        return status;
    }

    public void setStatus(MedicineStatus status) {
        this.status = status;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public OffsetDateTime getLastSoldAt() {
        return lastSoldAt;
    }

    public void setLastSoldAt(OffsetDateTime lastSoldAt) {
        this.lastSoldAt = lastSoldAt;
    }

    public OffsetDateTime getLastRestockedAt() {
        return lastRestockedAt;
    }

    public void setLastRestockedAt(OffsetDateTime lastRestockedAt) {
        this.lastRestockedAt = lastRestockedAt;
    }
}
