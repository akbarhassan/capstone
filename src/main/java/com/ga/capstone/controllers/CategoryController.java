package com.ga.capstone.controllers;


import com.ga.capstone.models.Category;
import com.ga.capstone.response.SuccessResponse;
import com.ga.capstone.services.CategoryService;
import com.ga.capstone.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller for Category CRUD operations and image upload.
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Create a new category.
     *
     * @param category the category to create
     * @return the created category
     */
    @PostMapping
    @PreAuthorize("hasAuthority('category:create')")
    public ResponseEntity<SuccessResponse> createCategory(@RequestBody Category category) {
        Category created = categoryService.createCategory(category);
        return ResponseBuilder.success(HttpStatus.CREATED, "Category created successfully", created);
    }

    /**
     * Get all categories.
     *
     * @return list of categories
     */
    @GetMapping
    @PreAuthorize("hasAuthority('category:read')")
    public ResponseEntity<SuccessResponse> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseBuilder.success(HttpStatus.OK, "Categories retrieved successfully", categories);
    }

    /**
     * Get a single category by ID.
     *
     * @param id the category ID
     * @return the category
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('category:read')")
    public ResponseEntity<SuccessResponse> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.findCategoryById(id);
        return ResponseBuilder.success(HttpStatus.OK, "Category retrieved successfully", category);
    }

    /**
     * Update an existing category.
     *
     * @param id       the category ID
     * @param category the updated data
     * @return the updated category
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('category:update')")
    public ResponseEntity<SuccessResponse> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Category updated = categoryService.updateCategory(id, category);
        return ResponseBuilder.success(HttpStatus.OK, "Category updated successfully", updated);
    }

    /**
     * Upload a category image.
     *
     * @param id   the category ID
     * @param file the image file
     * @return the updated category
     */
    @PostMapping("/{id}/image")
    @PreAuthorize("hasAuthority('category:update')")
    public ResponseEntity<SuccessResponse> uploadCategoryImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Category updated = categoryService.uploadCategoryImage(id, file);
        return ResponseBuilder.success(HttpStatus.OK, "Category image uploaded successfully", updated);
    }

    /**
     * Delete a category.
     *
     * @param id the category ID
     * @return success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('category:delete')")
    public ResponseEntity<SuccessResponse> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategoryById(id);
        return ResponseBuilder.success(HttpStatus.OK, "Category deleted successfully", null);
    }
}
