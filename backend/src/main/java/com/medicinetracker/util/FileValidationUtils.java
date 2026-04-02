package com.medicinetracker.util;

import java.util.ArrayList;
import java.util.List;

import com.medicinetracker.exception.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

public final class FileValidationUtils {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png", "image/webp", "text/csv", "application/vnd.ms-excel");

    private FileValidationUtils() {
    }

    public static void validateNotEmpty(MultipartFile file, String message) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(message);
        }
    }

    public static void validateContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !new ArrayList<>(ALLOWED_CONTENT_TYPES).contains(contentType)) {
            throw new BadRequestException("Unsupported file type: " + contentType);
        }
    }
}
