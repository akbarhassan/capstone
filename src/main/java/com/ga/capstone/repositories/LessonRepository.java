package com.ga.capstone.repositories;


import com.ga.capstone.models.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Lesson entity CRUD and custom queries.
 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    /**
     * Find all lessons belonging to a specific course.
     *
     * @param courseId the course ID
     * @return list of lessons
     */
    List<Lesson> findByCourseId(Long courseId);

    /**
     * Find all lessons for a course, ordered by sortOrder ascending.
     *
     * @param courseId the course ID
     * @return ordered list of lessons
     */
    List<Lesson> findByCourseIdOrderBySortOrderAsc(Long courseId);
}
