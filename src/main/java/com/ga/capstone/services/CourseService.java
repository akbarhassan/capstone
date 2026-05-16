package com.ga.capstone.services;


import com.ga.capstone.exceptions.ResourceNotFoundException;
import com.ga.capstone.models.Category;
import com.ga.capstone.models.Course;
import com.ga.capstone.models.User;
import com.ga.capstone.repositories.CategoryRepository;
import com.ga.capstone.repositories.CourseRepository;
import com.ga.capstone.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service handling CRUD operations for Course entities.
 * Supports soft-delete and thumbnail file uploads.
 */
@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    /**
     * Create a new course.
     *
     * @param course       the course to create
     * @param categoryId   the category ID to assign
     * @param instructorId the instructor (User) ID
     * @return the saved course
     */
    @Transactional
    public Course createCourse(Course course, Long categoryId, Long instructorId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new ResourceNotFoundException("Category not found with id: " + categoryId)
        );
        User instructor = userRepository.findById(instructorId).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id: " + instructorId)
        );

        course.setCategory(category);
        course.setInstructor(instructor);
        course.setDeleted(false);

        return courseRepository.save(course);
    }

    /**
     * Get all active (non-deleted) courses.
     *
     * @return list of active courses
     */
    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findByDeletedFalse();
    }

    /**
     * Get a course by ID (only if not deleted).
     *
     * @param id the course ID
     * @return the course
     * @throws ResourceNotFoundException if not found or deleted
     */
    @Transactional(readOnly = true)
    public Course getCourseById(Long id) {
        return courseRepository.findByIdAndDeletedFalse(id).orElseThrow(
                () -> new ResourceNotFoundException("Course not found with id: " + id)
        );
    }

    /**
     * Update an existing course.
     *
     * @param id     the course ID
     * @param course the updated course data
     * @return the updated course
     */
    @Transactional
    public Course updateCourse(Long id, Course course) {
        Course currentCourse = courseRepository.findByIdAndDeletedFalse(id).orElseThrow(
                () -> new ResourceNotFoundException("Course not found with id: " + id)
        );

        if (course.getTitle() != null) {
            currentCourse.setTitle(course.getTitle());
        }
        if (course.getDescription() != null) {
            currentCourse.setDescription(course.getDescription());
        }

        return courseRepository.save(currentCourse);
    }

    /**
     * Soft-delete a course (sets deleted flag to true).
     *
     * @param id the course ID
     */
    @Transactional
    public void softDeleteCourse(Long id) {
        Course course = courseRepository.findByIdAndDeletedFalse(id).orElseThrow(
                () -> new ResourceNotFoundException("Course not found with id: " + id)
        );

        course.setDeleted(true);
        courseRepository.save(course);
    }

    /**
     * Upload a course thumbnail image.
     *
     * @param courseId the course ID
     * @param file    the image file
     * @return the updated course
     */
    @Transactional
    public Course uploadCourseThumbnail(Long courseId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        Course course = courseRepository.findByIdAndDeletedFalse(courseId).orElseThrow(
                () -> new ResourceNotFoundException("Course not found with id: " + courseId)
        );

        String fileName = fileStorageService.saveFile(file, courseId, "courses");
        course.setThumbnail(fileName);

        return courseRepository.save(course);
    }

    /**
     * Get all courses by a specific instructor.
     *
     * @param instructorId the instructor user ID
     * @return list of courses
     */
    @Transactional(readOnly = true)
    public List<Course> getCoursesByInstructor(Long instructorId) {
        User instructor = userRepository.findById(instructorId).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id: " + instructorId)
        );
        return courseRepository.findByInstructorAndDeletedFalse(instructor);
    }

    /**
     * Get all courses in a specific category.
     *
     * @param categoryId the category ID
     * @return list of courses
     */
    @Transactional(readOnly = true)
    public List<Course> getCoursesByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new ResourceNotFoundException("Category not found with id: " + categoryId)
        );
        return courseRepository.findByCategory(category);
    }
}
