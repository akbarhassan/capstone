package com.ga.capstone.services;


import com.ga.capstone.exceptions.ResourceNotFoundException;
import com.ga.capstone.models.Course;
import com.ga.capstone.models.Lesson;
import com.ga.capstone.repositories.CourseRepository;
import com.ga.capstone.repositories.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service handling CRUD operations for Lesson entities.
 * Lessons are always scoped to a specific course.
 */
@Service
@RequiredArgsConstructor
public class LessonService {
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    /**
     * Create a new lesson in a specific course.
     *
     * @param courseId the course ID
     * @param lesson  the lesson to create
     * @return the saved lesson
     */
    @Transactional
    public Lesson createLesson(Long courseId, Lesson lesson) {
        Course course = courseRepository.findByIdAndDeletedFalse(courseId).orElseThrow(
                () -> new ResourceNotFoundException("Course not found with id: " + courseId)
        );

        lesson.setCourse(course);
        return lessonRepository.save(lesson);
    }

    /**
     * Get all lessons for a course, ordered by sortOrder.
     *
     * @param courseId the course ID
     * @return ordered list of lessons
     */
    @Transactional(readOnly = true)
    public List<Lesson> getLessonsByCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }
        return lessonRepository.findByCourseIdOrderBySortOrderAsc(courseId);
    }

    /**
     * Get a single lesson by ID.
     *
     * @param id the lesson ID
     * @return the lesson
     * @throws ResourceNotFoundException if not found
     */
    @Transactional(readOnly = true)
    public Lesson getLessonById(Long id) {
        return lessonRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Lesson not found with id: " + id)
        );
    }

    /**
     * Update an existing lesson.
     *
     * @param id     the lesson ID
     * @param lesson the updated lesson data
     * @return the updated lesson
     */
    @Transactional
    public Lesson updateLesson(Long id, Lesson lesson) {
        Lesson currentLesson = lessonRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Lesson not found with id: " + id)
        );

        if (lesson.getTitle() != null) {
            currentLesson.setTitle(lesson.getTitle());
        }
        if (lesson.getContent() != null) {
            currentLesson.setContent(lesson.getContent());
        }
        if (lesson.getVideoUrl() != null) {
            currentLesson.setVideoUrl(lesson.getVideoUrl());
        }
        if (lesson.getSortOrder() != null) {
            currentLesson.setSortOrder(lesson.getSortOrder());
        }

        return lessonRepository.save(currentLesson);
    }

    /**
     * Delete a lesson by ID.
     *
     * @param id the lesson ID
     */
    @Transactional
    public void deleteLesson(Long id) {
        Lesson lesson = lessonRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Lesson not found with id: " + id)
        );
        lessonRepository.delete(lesson);
    }
}
