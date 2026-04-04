package com.conghoan.sportbooking.controller;

import com.conghoan.sportbooking.dto.ApiResponse;
import com.conghoan.sportbooking.entity.SportCategory;
import com.conghoan.sportbooking.repository.SportCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final SportCategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SportCategory>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(categoryRepository.findByActiveTrue()));
    }
}
