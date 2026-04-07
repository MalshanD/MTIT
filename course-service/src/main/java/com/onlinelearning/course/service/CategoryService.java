package com.onlinelearning.course.service;

import com.onlinelearning.course.dto.*;
import com.onlinelearning.course.entity.CourseCategory;
import com.onlinelearning.course.repository.CourseCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CourseCategoryRepository categoryRepository;

    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Category '" + request.getName() + "' already exists");
        }

        CourseCategory category = CourseCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .iconUrl(request.getIconUrl())
                .build();

        category = categoryRepository.save(category);
        return mapToResponse(category);
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(Long categoryId) {
        CourseCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));
        return mapToResponse(category);
    }

    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request) {
        CourseCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIconUrl(request.getIconUrl());

        category = categoryRepository.save(category);
        return mapToResponse(category);
    }

    public String deleteCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new RuntimeException("Category not found with ID: " + categoryId);
        }
        categoryRepository.deleteById(categoryId);
        return "Category with ID " + categoryId + " has been deleted";
    }

    private CategoryResponse mapToResponse(CourseCategory category) {
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .description(category.getDescription())
                .iconUrl(category.getIconUrl())
                .build();
    }
}
