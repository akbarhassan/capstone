package com.ga.capstone.services;


import com.ga.capstone.exceptions.ResourceAlreadyExistsException;
import com.ga.capstone.exceptions.ResourceNotFoundException;
import com.ga.capstone.models.Category;
import com.ga.capstone.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private CategoryService categoryService;

    // ============ TEST DATA ============
    private Category category1;
    private Category category2;

    // ============ SETUP ============
    @BeforeEach
    void setUp() {
        category1 = new Category();
        category1.setId(1L);
        category1.setName("Programming");
        category1.setDescription("Learn programming");

        category2 = new Category();
        category2.setId(2L);
        category2.setName("Design");
        category2.setDescription("Learn design");
    }

    // ============ TEST: getAllCategories ============
    @Test
    @DisplayName("getAllCategories: returns list of all categories")
    void getAllCategories_returnsAllCategories() {
        List<Category> expectedCategories = Arrays.asList(category1, category2);

        when(categoryRepository.findAll()).thenReturn(expectedCategories);

        List<Category> result = categoryService.getAllCategories();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(category1, category2);

        verify(categoryRepository, times(1)).findAll();
        verifyNoMoreInteractions(categoryRepository);
    }

    // ============ TEST: findCategoryById ============
    @Test
    @DisplayName("findCategoryById: returns a category based on ID")
    void findCategoryById_returnsCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));

        Category result = categoryService.findCategoryById(1L);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(category1);

        verify(categoryRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("findCategoryById: non-existing ID → throws ResourceNotFoundException")
    void findCategoryById_nonExistingId_throwsResourceNotFoundException() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findCategoryById(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, times(1)).findById(999L);
        verifyNoMoreInteractions(categoryRepository);
    }

    // ============ TEST: createCategory ============
    @Test
    @DisplayName("createCategory: unique name → saves and returns category")
    void createCategory_uniqueName_savesAndReturnsCategory() {
        Category newCategory = new Category();
        newCategory.setName("Books");
        newCategory.setDescription("Books items");

        when(categoryRepository.existsByName("Books")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        Category result = categoryService.createCategory(newCategory);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Books");
        assertThat(result.getDescription()).isEqualTo("Books items");

        verify(categoryRepository, times(1)).existsByName("Books");
        verify(categoryRepository, times(1)).save(newCategory);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("createCategory: duplicate name → throws ResourceAlreadyExistsException")
    void createCategory_duplicateName_throwsResourceAlreadyExistsException() {
        Category duplicateCategory = new Category();
        duplicateCategory.setName("Programming");

        when(categoryRepository.existsByName("Programming")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(duplicateCategory))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("Programming");

        verify(categoryRepository, times(1)).existsByName("Programming");
        verify(categoryRepository, never()).save(any(Category.class));
        verifyNoMoreInteractions(categoryRepository);
    }

    // ============ TEST: updateCategory ============
    @Test
    @DisplayName("updateCategory: valid name + description → updates and returns category")
    void updateCategory_validNameAndDescription_updatesAndReturnsCategory() {
        Category updateRequest = new Category();
        updateRequest.setName("ProgrammingNew");
        updateRequest.setDescription("Programming updated");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.existsByName("ProgrammingNew")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        Category result = categoryService.updateCategory(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("ProgrammingNew");
        assertThat(result.getDescription()).isEqualTo("Programming updated");

        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).existsByName("ProgrammingNew");
        verify(categoryRepository, times(1)).save(category1);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("updateCategory: duplicate name → throws ResourceAlreadyExistsException")
    void updateCategory_duplicateName_throwsResourceAlreadyExistsException() {
        Category duplicateCategory = new Category();
        duplicateCategory.setName("Design");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.existsByName("Design")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.updateCategory(1L, duplicateCategory))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("Design");

        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).existsByName("Design");
        verify(categoryRepository, never()).save(any(Category.class));
        verifyNoMoreInteractions(categoryRepository);
    }

    // ============ TEST: deleteCategoryById ============
    @Test
    @DisplayName("deleteCategoryById: existing ID → deletes category")
    void deleteCategoryById_existingId_deletesCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));

        categoryService.deleteCategoryById(1L);

        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).deleteById(1L);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("deleteCategoryById: missing ID → throws ResourceNotFoundException")
    void deleteCategoryById_missingId_throwsResourceNotFoundException() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategoryById(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, times(1)).findById(999L);
        verify(categoryRepository, never()).deleteById(anyLong());
        verifyNoMoreInteractions(categoryRepository);
    }

    // ============ TEST: uploadCategoryImage ============
    @Test
    @DisplayName("uploadCategoryImage: valid file → uploads and returns updated category")
    void uploadCategoryImage_validFile_uploadsAndReturnsCategory() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.png", "image/png", "data".getBytes());

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(fileStorageService.saveFile(any(), eq(1L), eq("categories"))).thenReturn("1_123.png");
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        Category result = categoryService.uploadCategoryImage(1L, file);

        assertThat(result).isNotNull();
        assertThat(result.getCategoryImage()).isEqualTo("1_123.png");

        verify(categoryRepository, times(1)).findById(1L);
        verify(fileStorageService, times(1)).saveFile(any(), eq(1L), eq("categories"));
        verify(categoryRepository, times(1)).save(category1);
    }
}
