package com.ga.capstone.repositories;


import com.ga.capstone.models.Category;
import com.ga.capstone.models.Course;
import com.ga.capstone.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Course entity CRUD and custom queries.
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * Find all courses that are not soft-deleted.
     *
     * @return list of active courses
     */
    List<Course> findByDeletedFalse();

    /**
     * Find a course by ID only if it is not soft-deleted.
     *
     * @param id the course ID
     * @return optional Course
     */
    Optional<Course> findByIdAndDeletedFalse(Long id);

    /**
     * Find all courses by a specific instructor.
     *
     * @param instructor the instructor user
     * @return list of courses
     */
    List<Course> findByInstructor(User instructor);

    /**
     * Find all courses in a specific category.
     *
     * @param category the category
     * @return list of courses
     */
    List<Course> findByCategory(Category category);

    /**
     * Find all active courses by instructor.
     *
     * @param instructor the instructor user
     * @return list of non-deleted courses
     */
    List<Course> findByInstructorAndDeletedFalse(User instructor);
}
