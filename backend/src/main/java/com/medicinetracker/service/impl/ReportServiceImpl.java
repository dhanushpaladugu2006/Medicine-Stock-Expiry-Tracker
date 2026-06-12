package com.medicinetracker.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.medicinetracker.dto.report.ReportFilterRequest;
import com.medicinetracker.entity.Medicine;
import com.medicinetracker.entity.StockTransaction;
import com.medicinetracker.entity.User;
import com.medicinetracker.entity.enums.AuditAction;
import com.medicinetracker.entity.enums.Role;
import com.medicinetracker.exception.BadRequestException;
import com.medicinetracker.repository.MedicineRepository;
import com.medicinetracker.repository.StockTransactionRepository;
import com.medicinetracker.service.AuditService;
import com.medicinetracker.service.ReportService;
import com.medicinetracker.util.AuthenticatedUserProvider;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final MedicineRepository medicineRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final AuditService auditService;
    private final MongoTemplate mongoTemplate;

    public ReportServiceImpl(MedicineRepository medicineRepository, StockTransactionRepository stockTransactionRepository, AuthenticatedUserProvider authenticatedUserProvider, AuditService auditService, MongoTemplate mongoTemplate) {
        this.medicineRepository = medicineRepository;
        this.stockTransactionRepository = stockTransactionRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.auditService = auditService;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public byte[] exportReport(String type, String format, ReportFilterRequest filter) {
        UUID branchId = resolveBranch(filter.branchId());
        LocalDate fromDate = filter.fromDate() != null ? filter.fromDate() : LocalDate.now().minusDays(30);
        LocalDate toDate = filter.toDate() != null ? filter.toDate() : LocalDate.now().plusDays(30);

        byte[] output = switch (format.toLowerCase()) {
            case "csv" -> exportCsv(type, branchId, fromDate, toDate);
            case "pdf" -> exportPdf(type, branchId, fromDate, toDate);
            default -> throw new BadRequestException("Unsupported report format");
        };
        auditService.record(AuditAction.EXPORT, "REPORT", type, "Report exported", "format=" + format);
        return output;
    }

    private UUID resolveBranch(UUID requestedBranchId) {
        User user = authenticatedUserProvider.getCurrentUser();
        if (user.getRole() == Role.ADMIN) {
            return requestedBranchId;
        }
        if (user.getBranch() == null) {
            throw new BadRequestException("User is not mapped to a branch");
        }
        if (requestedBranchId != null && !requestedBranchId.equals(user.getBranch().getId())) {
            throw new BadRequestException("You can only export reports for your branch");
        }
        return user.getBranch().getId();
    }

    private byte[] exportCsv(String type, UUID branchId, LocalDate fromDate, LocalDate toDate) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            switch (type.toLowerCase()) {
                case "expiry" -> {
                    printer.printRecord("Name", "Batch", "Branch", "Expiry Date", "Quantity", "Status");
                    for (Medicine medicine : findMedicines(branchId, fromDate, toDate)) {
                        printer.printRecord(medicine.getName(), medicine.getBatchNumber(), medicine.getBranch().getName(), medicine.getExpiryDate(), medicine.getQuantity(), medicine.getStatus());
                    }
                }
                case "stock" -> {
                    printer.printRecord("Name", "Batch", "Branch", "Quantity", "Reorder Level", "Price", "Status");
                    for (Medicine medicine : findMedicines(branchId, null, null)) {
                        printer.printRecord(medicine.getName(), medicine.getBatchNumber(), medicine.getBranch().getName(), medicine.getQuantity(), medicine.getReorderLevel(), medicine.getPrice(), medicine.getStatus());
                    }
                }
                case "usage" -> {
                    printer.printRecord("Medicine", "Branch", "Type", "Quantity Change", "Performed By", "Transaction Date");
                    List<StockTransaction> transactions = stockTransactionRepository.findByDateRange(fromDate.atStartOfDay().atOffset(OffsetDateTime.now().getOffset()), toDate.plusDays(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset()));
                    for (StockTransaction transaction : transactions) {
                        if (branchId == null || transaction.getBranch().getId().equals(branchId)) {
                            printer.printRecord(transaction.getMedicine().getName(), transaction.getBranch().getName(), transaction.getType(), transaction.getQuantityChange(), transaction.getPerformedBy() != null ? transaction.getPerformedBy().getFullName() : "System", transaction.getTransactionDate());
                        }
                    }
                }
                default -> throw new BadRequestException("Unsupported report type");
            }
            printer.flush();
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new BadRequestException("Failed to generate CSV report");
        }
    }

    private byte[] exportPdf(String type, UUID branchId, LocalDate fromDate, LocalDate toDate) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();
            document.add(new Paragraph(type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase() + " Report", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph("Generated on " + OffsetDateTime.now()));
            document.add(new Paragraph(" "));

            switch (type.toLowerCase()) {
                case "expiry" -> addMedicineTable(document, findMedicines(branchId, fromDate, toDate), true);
                case "stock" -> addMedicineTable(document, findMedicines(branchId, null, null), false);
                case "usage" -> addUsageTable(document, branchId, fromDate, toDate);
                default -> throw new BadRequestException("Unsupported report type");
            }
            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException | IOException exception) {
            throw new BadRequestException("Failed to generate PDF report");
        }
    }

    private void addMedicineTable(Document document, List<Medicine> medicines, boolean includeExpiry) throws DocumentException {
        PdfPTable table = new PdfPTable(includeExpiry ? 6 : 6);
        addCell(table, "Medicine");
        addCell(table, "Batch");
        addCell(table, "Branch");
        addCell(table, includeExpiry ? "Expiry" : "Quantity");
        addCell(table, includeExpiry ? "Quantity" : "Reorder");
        addCell(table, "Status");
        for (Medicine medicine : medicines) {
            addCell(table, medicine.getName());
            addCell(table, medicine.getBatchNumber());
            addCell(table, medicine.getBranch().getName());
            addCell(table, includeExpiry ? medicine.getExpiryDate().toString() : String.valueOf(medicine.getQuantity()));
            addCell(table, includeExpiry ? String.valueOf(medicine.getQuantity()) : String.valueOf(medicine.getReorderLevel()));
            addCell(table, medicine.getStatus().name());
        }
        document.add(table);
    }

    private void addUsageTable(Document document, UUID branchId, LocalDate fromDate, LocalDate toDate) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        addCell(table, "Medicine");
        addCell(table, "Branch");
        addCell(table, "Type");
        addCell(table, "Quantity Change");
        addCell(table, "Date");
        List<StockTransaction> transactions = stockTransactionRepository.findByDateRange(fromDate.atStartOfDay().atOffset(OffsetDateTime.now().getOffset()), toDate.plusDays(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset()));
        for (StockTransaction transaction : transactions) {
            if (branchId == null || transaction.getBranch().getId().equals(branchId)) {
                addCell(table, transaction.getMedicine().getName());
                addCell(table, transaction.getBranch().getName());
                addCell(table, transaction.getType().name());
                addCell(table, String.valueOf(transaction.getQuantityChange()));
                addCell(table, transaction.getTransactionDate().toLocalDate().toString());
            }
        }
        document.add(table);
    }

    private void addCell(PdfPTable table, String value) {
        table.addCell(new PdfPCell(new Phrase(value)));
    }

    private List<Medicine> findMedicines(UUID branchId, LocalDate fromDate, LocalDate toDate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("archived").is(false));
        if (branchId != null) {
            query.addCriteria(Criteria.where("branch").is(branchId));
        }
        if (fromDate != null && toDate != null) {
            query.addCriteria(Criteria.where("expiryDate").gte(fromDate).lte(toDate));
        } else if (fromDate != null) {
            query.addCriteria(Criteria.where("expiryDate").gte(fromDate));
        } else if (toDate != null) {
            query.addCriteria(Criteria.where("expiryDate").lte(toDate));
        }
        return mongoTemplate.find(query, Medicine.class);
    }
}

