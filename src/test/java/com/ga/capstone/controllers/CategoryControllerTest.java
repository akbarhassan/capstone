package com.ga.capstone.controllers;


import com.ga.capstone.models.Category;
import com.ga.capstone.response.SuccessResponse;
import com.ga.capstone.services.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    // ============ TEST DATA ============
    private Category category1;
    private Category category2;

    // ============ SETUP ============
    @BeforeEach
    void setUp() {
        category1 = new Category();
        category1.setId(1L);
        category1.setName("Programming");
        category1.setDescription("Learn to code");

        category2 = new Category();
        category2.setId(2L);
        category2.setName("Design");
        category2.setDescription("Learn design");
    }

    // ============ TEST: createCategory ============
    @Test
    @DisplayName("createCategory: valid category → returns CREATED with category data")
    void createCategory_validCategory_returnsCreated() {
        when(categoryService.createCategory(any(Category.class))).thenReturn(category1);

        ResponseEntity<SuccessResponse> result = categoryController.createCategory(category1);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Category created successfully");
        assertThat(result.getBody().data()).isEqualTo(category1);

        verify(categoryService, times(1)).createCategory(category1);
    }

    // ============ TEST: getAllCategories ============
    @Test
    @DisplayName("getAllCategories: returns list of categories")
    void getAllCategories_returnsListOfCategories() {
        List<Category> categories = Arrays.asList(category1, category2);
        when(categoryService.getAllCategories()).thenReturn(categories);

        ResponseEntity<SuccessResponse> result = categoryController.getAllCategories();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Categories retrieved successfully");
        assertThat(result.getBody().data()).isEqualTo(categories);

        verify(categoryService, times(1)).getAllCategories();
    }

    // ============ TEST: getCategoryById ============
    @Test
    @DisplayName("getCategoryById: returns category by ID")
    void getCategoryById_returnsCategoryById() {
        when(categoryService.findCategoryById(1L)).thenReturn(category1);

        ResponseEntity<SuccessResponse> result = categoryController.getCategoryById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Category retrieved successfully");
        assertThat(result.getBody().data()).isEqualTo(category1);

        verify(categoryService, times(1)).findCategoryById(1L);
    }

    // ============ TEST: updateCategory ============
    @Test
    @DisplayName("updateCategory: updates and returns category")
    void updateCategory_updatesAndReturnsCategory() {
        Category updateRequest = new Category();
        updateRequest.setName("Updated Name");

        Category updatedCategory = new Category();
        updatedCategory.setId(1L);
        updatedCategory.setName("Updated Name");

        when(categoryService.updateCategory(eq(1L), any(Category.class))).thenReturn(updatedCategory);

        ResponseEntity<SuccessResponse> result = categoryController.updateCategory(1L, updateRequest);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Category updated successfully");
        assertThat(result.getBody().data()).isEqualTo(updatedCategory);

        verify(categoryService, times(1)).updateCategory(1L, updateRequest);
    }

    // ============ TEST: deleteCategory ============
    @Test
    @DisplayName("deleteCategory: deletes category and returns success")
    void deleteCategory_deletesCategory() {
        doNothing().when(categoryService).deleteCategoryById(1L);

        ResponseEntity<SuccessResponse> result = categoryController.deleteCategory(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Category deleted successfully");
        assertThat(result.getBody().data()).isNull();

        verify(categoryService, times(1)).deleteCategoryById(1L);
    }

    // ============ TEST: uploadCategoryImage ============
    @Test
    @DisplayName("uploadCategoryImage: uploads image and returns updated category")
    void uploadCategoryImage_uploadsImage() {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", "fake-image-data".getBytes());

        Category updatedCategory = new Category();
        updatedCategory.setId(1L);
        updatedCategory.setCategoryImage("1_123456.png");

        when(categoryService.uploadCategoryImage(eq(1L), any())).thenReturn(updatedCategory);

        ResponseEntity<SuccessResponse> result = categoryController.uploadCategoryImage(1L, file);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Category image uploaded successfully");
        assertThat(result.getBody().data()).isEqualTo(updatedCategory);

        verify(categoryService, times(1)).uploadCategoryImage(eq(1L), any());
    }
}
