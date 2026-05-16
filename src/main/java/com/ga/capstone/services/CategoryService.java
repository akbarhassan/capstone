package com.ga.capstone.services;


import com.ga.capstone.exceptions.ResourceAlreadyExistsException;
import com.ga.capstone.exceptions.ResourceNotFoundException;
import com.ga.capstone.models.Category;
import com.ga.capstone.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service handling CRUD operations for Category entities.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;


    /**
     * Create a new category.
     *
     * @param category the category to create
     * @return the saved category
     * @throws ResourceAlreadyExistsException if name already exists
     */
    @Transactional
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new ResourceAlreadyExistsException("Category with name " + category.getName() + " already exists");
        }
        return categoryRepository.save(category);
    }


    /**
     * Get all categories.
     *
     * @return list of all categories
     */
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }


    /**
     * Find a category by its ID.
     *
     * @param id the category ID
     * @return the found category
     * @throws ResourceNotFoundException if not found
     */
    @Transactional(readOnly = true)
    public Category findCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("No Category exists with id: " + id)
        );
    }


    /**
     * Update an existing category.
     *
     * @param id       the category ID
     * @param category the updated category data
     * @return the updated category
     */
    @Transactional
    public Category updateCategory(Long id, Category category) {
        Category currentCategory = categoryRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("No category exists with id: " + id)
        );

        if (category.getName() != null) {
            if (!currentCategory.getName().equals(category.getName())) {
                if (categoryRepository.existsByName(category.getName())) {
                    throw new ResourceAlreadyExistsException("Category with name " + category.getName() + " already exists");
                }
                currentCategory.setName(category.getName());
            }
        }
        if (category.getDescription() != null) {
            currentCategory.setDescription(category.getDescription());
        }

        return categoryRepository.save(currentCategory);
    }

    /**
     * Upload a category image.
     *
     * @param categoryId the category ID
     * @param file       the image file to upload
     * @return the updated category
     */
    @Transactional
    public Category uploadCategoryImage(Long categoryId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        Category currentCategory = categoryRepository.findById(categoryId).orElseThrow(
                () -> new ResourceNotFoundException("No category exists with id: " + categoryId)
        );

        String fileName = fileStorageService.saveFile(file, categoryId, "categories");

        currentCategory.setCategoryImage(fileName);

        return categoryRepository.save(currentCategory);
    }


    /**
     * Delete a category by ID. Also removes associated image file.
     *
     * @param id the category ID
     */
    @Transactional
    public void deleteCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("No category exists with id: " + id)
        );

        if (category.getCategoryImage() != null) {
            fileStorageService.deleteFile(category.getCategoryImage(), "categories");
        }

        categoryRepository.deleteById(id);
    }
}
