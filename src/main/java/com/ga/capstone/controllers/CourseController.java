package com.ga.capstone.controllers;


import com.ga.capstone.models.Course;
import com.ga.capstone.response.SuccessResponse;
import com.ga.capstone.security.MyUserDetails;
import com.ga.capstone.services.CourseService;
import com.ga.capstone.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller for Course CRUD operations, thumbnail upload, and filtering.
 */
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * Create a new course. The authenticated user becomes the instructor.
     *
     * @param course     the course data
     * @param categoryId the category to assign
     * @param userDetails the authenticated user (instructor)
     * @return the created course
     */
    @PostMapping
    @PreAuthorize("hasAuthority('course:create')")
    public ResponseEntity<SuccessResponse> createCourse(
            @RequestBody Course course,
            @RequestParam Long categoryId,
            @AuthenticationPrincipal MyUserDetails userDetails) {
        Course created = courseService.createCourse(course, categoryId, userDetails.getUser().getId());
        return ResponseBuilder.success(HttpStatus.CREATED, "Course created successfully", created);
    }

    /**
     * Get all active courses.
     *
     * @return list of courses
     */
    @GetMapping
    @PreAuthorize("hasAuthority('course:read')")
    public ResponseEntity<SuccessResponse> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        return ResponseBuilder.success(HttpStatus.OK, "Courses retrieved successfully", courses);
    }

    /**
     * Get a single course by ID.
     *
     * @param id the course ID
     * @return the course
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('course:read')")
    public ResponseEntity<SuccessResponse> getCourseById(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        return ResponseBuilder.success(HttpStatus.OK, "Course retrieved successfully", course);
    }

    /**
     * Update a course.
     *
     * @param id     the course ID
     * @param course the updated data
     * @return the updated course
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('course:update')")
    public ResponseEntity<SuccessResponse> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        Course updated = courseService.updateCourse(id, course);
        return ResponseBuilder.success(HttpStatus.OK, "Course updated successfully", updated);
    }

    /**
     * Upload a course thumbnail image.
     *
     * @param id   the course ID
     * @param file the image file
     * @return the updated course
     */
    @PostMapping("/{id}/thumbnail")
    @PreAuthorize("hasAuthority('course:update')")
    public ResponseEntity<SuccessResponse> uploadThumbnail(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Course updated = courseService.uploadCourseThumbnail(id, file);
        return ResponseBuilder.success(HttpStatus.OK, "Course thumbnail uploaded successfully", updated);
    }

    /**
     * Soft-delete a course.
     *
     * @param id the course ID
     * @return success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('course:delete')")
    public ResponseEntity<SuccessResponse> deleteCourse(@PathVariable Long id) {
        courseService.softDeleteCourse(id);
        return ResponseBuilder.success(HttpStatus.OK, "Course deleted successfully", null);
    }

    /**
     * Get courses by the authenticated instructor.
     *
     * @param userDetails the authenticated user
     * @return list of courses
     */
    @GetMapping("/my-courses")
    @PreAuthorize("hasAuthority('course:read')")
    public ResponseEntity<SuccessResponse> getMyCourses(@AuthenticationPrincipal MyUserDetails userDetails) {
        List<Course> courses = courseService.getCoursesByInstructor(userDetails.getUser().getId());
        return ResponseBuilder.success(HttpStatus.OK, "Courses retrieved successfully", courses);
    }

    /**
     * Get all courses in a specific category.
     *
     * @param categoryId the category ID
     * @return list of courses
     */
    @GetMapping("/by-category/{categoryId}")
    @PreAuthorize("hasAuthority('course:read')")
    public ResponseEntity<SuccessResponse> getCoursesByCategory(@PathVariable Long categoryId) {
        List<Course> courses = courseService.getCoursesByCategory(categoryId);
        return ResponseBuilder.success(HttpStatus.OK, "Courses retrieved successfully", courses);
    }
}
