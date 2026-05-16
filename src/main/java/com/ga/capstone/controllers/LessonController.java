package com.ga.capstone.controllers;


import com.ga.capstone.models.Lesson;
import com.ga.capstone.response.SuccessResponse;
import com.ga.capstone.services.LessonService;
import com.ga.capstone.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Lesson CRUD operations, nested under a course.
 */
@RestController
@RequestMapping("/api/v1/courses/{courseId}/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    /**
     * Create a new lesson in a course.
     *
     * @param courseId the course ID
     * @param lesson   the lesson data
     * @return the created lesson
     */
    @PostMapping
    @PreAuthorize("hasAuthority('lesson:create')")
    public ResponseEntity<SuccessResponse> createLesson(@PathVariable Long courseId, @RequestBody Lesson lesson) {
        Lesson created = lessonService.createLesson(courseId, lesson);
        return ResponseBuilder.success(HttpStatus.CREATED, "Lesson created successfully", created);
    }

    /**
     * Get all lessons for a course (ordered by sortOrder).
     *
     * @param courseId the course ID
     * @return list of lessons
     */
    @GetMapping
    @PreAuthorize("hasAuthority('lesson:read')")
    public ResponseEntity<SuccessResponse> getLessonsByCourse(@PathVariable Long courseId) {
        List<Lesson> lessons = lessonService.getLessonsByCourse(courseId);
        return ResponseBuilder.success(HttpStatus.OK, "Lessons retrieved successfully", lessons);
    }

    /**
     * Get a single lesson by ID.
     *
     * @param courseId the course ID (for URL context)
     * @param id       the lesson ID
     * @return the lesson
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('lesson:read')")
    public ResponseEntity<SuccessResponse> getLessonById(@PathVariable Long courseId, @PathVariable Long id) {
        Lesson lesson = lessonService.getLessonById(id);
        return ResponseBuilder.success(HttpStatus.OK, "Lesson retrieved successfully", lesson);
    }

    /**
     * Update an existing lesson.
     *
     * @param courseId the course ID (for URL context)
     * @param id       the lesson ID
     * @param lesson   the updated data
     * @return the updated lesson
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('lesson:update')")
    public ResponseEntity<SuccessResponse> updateLesson(@PathVariable Long courseId, @PathVariable Long id, @RequestBody Lesson lesson) {
        Lesson updated = lessonService.updateLesson(id, lesson);
        return ResponseBuilder.success(HttpStatus.OK, "Lesson updated successfully", updated);
    }

    /**
     * Delete a lesson.
     *
     * @param courseId the course ID (for URL context)
     * @param id       the lesson ID
     * @return success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('lesson:delete')")
    public ResponseEntity<SuccessResponse> deleteLesson(@PathVariable Long courseId, @PathVariable Long id) {
        lessonService.deleteLesson(id);
        return ResponseBuilder.success(HttpStatus.OK, "Lesson deleted successfully", null);
    }
}
