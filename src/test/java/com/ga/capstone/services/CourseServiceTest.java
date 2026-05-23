package com.ga.capstone.services;


import com.ga.capstone.exceptions.ResourceNotFoundException;
import com.ga.capstone.models.Category;
import com.ga.capstone.models.Course;
import com.ga.capstone.models.User;
import com.ga.capstone.repositories.CategoryRepository;
import com.ga.capstone.repositories.CourseRepository;
import com.ga.capstone.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private CourseService courseService;

    // ============ TEST DATA ============
    private Course course1;
    private Category category;
    private User instructor;

    // ============ SETUP ============
    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Programming");

        instructor = new User();
        instructor.setId(1L);
        instructor.setEmail("instructor@test.com");

        course1 = new Course();
        course1.setId(1L);
        course1.setTitle("Java Basics");
        course1.setDescription("Learn Java");
        course1.setCategory(category);
        course1.setInstructor(instructor);
        course1.setDeleted(false);
    }

    // ============ TEST: createCourse ============
    @Test
    @DisplayName("createCourse: valid course → saves and returns course")
    void createCourse_validCourse_savesAndReturnsCourse() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(userRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(courseRepository.save(any(Course.class))).thenReturn(course1);

        Course result = courseService.createCourse(course1, 1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Java Basics");

        verify(categoryRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    @DisplayName("createCourse: category not found → throws ResourceNotFoundException")
    void createCourse_categoryNotFound_throwsResourceNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.createCourse(course1, 99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, times(1)).findById(99L);
        verify(courseRepository, never()).save(any(Course.class));
    }

    // ============ TEST: getAllCourses ============
    @Test
    @DisplayName("getAllCourses: returns list of active courses")
    void getAllCourses_returnsActiveCourses() {
        when(courseRepository.findByDeletedFalse()).thenReturn(Arrays.asList(course1));

        List<Course> result = courseService.getAllCourses();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Java Basics");

        verify(courseRepository, times(1)).findByDeletedFalse();
    }

    // ============ TEST: getCourseById ============
    @Test
    @DisplayName("getCourseById: existing ID → returns course")
    void getCourseById_existingId_returnsCourse() {
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course1));

        Course result = courseService.getCourseById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Java Basics");

        verify(courseRepository, times(1)).findByIdAndDeletedFalse(1L);
    }

    @Test
    @DisplayName("getCourseById: non-existing ID → throws ResourceNotFoundException")
    void getCourseById_nonExistingId_throwsResourceNotFoundException() {
        when(courseRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseById(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(courseRepository, times(1)).findByIdAndDeletedFalse(99L);
    }

    // ============ TEST: updateCourse ============
    @Test
    @DisplayName("updateCourse: valid update → updates and returns course")
    void updateCourse_validUpdate_updatesAndReturnsCourse() {
        Course updateData = new Course();
        updateData.setTitle("Advanced Java");

        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course1));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArgument(0));

        Course result = courseService.updateCourse(1L, updateData);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Advanced Java");

        verify(courseRepository, times(1)).findByIdAndDeletedFalse(1L);
        verify(courseRepository, times(1)).save(course1);
    }

    // ============ TEST: softDeleteCourse ============
    @Test
    @DisplayName("softDeleteCourse: existing ID → marks course as deleted")
    void softDeleteCourse_existingId_marksCourseAsDeleted() {
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course1));

        courseService.softDeleteCourse(1L);

        assertThat(course1.getDeleted()).isTrue();

        verify(courseRepository, times(1)).findByIdAndDeletedFalse(1L);
        verify(courseRepository, times(1)).save(course1);
    }

    // ============ TEST: getCoursesByInstructor ============
    @Test
    @DisplayName("getCoursesByInstructor: returns instructor's courses")
    void getCoursesByInstructor_returnsInstructorCourses() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(courseRepository.findByInstructorAndDeletedFalse(instructor))
                .thenReturn(Arrays.asList(course1));

        List<Course> result = courseService.getCoursesByInstructor(1L);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(userRepository, times(1)).findById(1L);
        verify(courseRepository, times(1)).findByInstructorAndDeletedFalse(instructor);
    }
}
