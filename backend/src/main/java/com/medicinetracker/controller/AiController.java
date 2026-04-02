package com.medicinetracker.controller;

import com.medicinetracker.dto.common.ApiResponse;
import com.medicinetracker.dto.common.ImageRecognitionPlaceholderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    @PostMapping("/recognition-placeholder")
    public ResponseEntity<ApiResponse<ImageRecognitionPlaceholderResponse>> recognizeMedicine(@RequestPart(value = "file", required = false) MultipartFile file) {
        String suggestedName = file != null && file.getOriginalFilename() != null
                ? file.getOriginalFilename().replaceAll("\\.[^.]+$", "").replace('-', ' ')
                : "No file submitted";
        return ResponseEntity.ok(ApiResponse.success(
                "Placeholder recognition executed",
                new ImageRecognitionPlaceholderResponse(
                        suggestedName,
                        file != null ? "0.42" : "0.00",
                        "This is a scaffold endpoint for future OCR/image-recognition integration."
                )
        ));
    }
}
