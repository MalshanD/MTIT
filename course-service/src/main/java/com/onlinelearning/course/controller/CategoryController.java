package com.onlinelearning.course.controller;

import com.onlinelearning.course.dto.*;
import com.onlinelearning.course.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/courses/categories")
@RequiredArgsConstructor
@Tag(name = "Course Categories", description = "Manage course categories (Programming, Design, Business, etc.)")
public class CategoryController {

    private final CategoryService categoryService;

    // ==================== ROLE HELPER ====================

    private void requireRole(String role, String... allowed) {
        for (String r : allowed) {
            if (r.equalsIgnoreCase(role)) return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Access denied. Required role: " + String.join(" or ", allowed));
    }

    // ==================== ENDPOINTS ====================

    @PostMapping
    @Operation(summary = "Create a new category",
            description = "Only ADMIN can create categories.")
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody CategoryRequest request) {
        requireRole(userRole, "ADMIN");
        CategoryResponse response = categoryService.createCategory(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> responses = categoryService.getAllCategories();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long categoryId) {
        CategoryResponse response = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{categoryId}")
    @Operation(summary = "Update a category",
            description = "Only ADMIN can update categories.")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long categoryId,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody CategoryRequest request) {
        requireRole(userRole, "ADMIN");
        CategoryResponse response = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Delete a category",
            description = "Only ADMIN can delete categories.")
    public ResponseEntity<String> deleteCategory(
            @PathVariable Long categoryId,
            @RequestHeader("X-User-Role") String userRole) {
        requireRole(userRole, "ADMIN");
        String message = categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(message);
    }
}
