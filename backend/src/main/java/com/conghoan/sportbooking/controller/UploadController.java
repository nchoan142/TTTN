package com.conghoan.sportbooking.controller;

import com.conghoan.sportbooking.dto.ApiResponse;
import com.conghoan.sportbooking.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/image")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = cloudinaryService.uploadImage(file, "sportbooking");
            return ResponseEntity.ok(ApiResponse.ok("Upload thành công", result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
