package com.ga.capstone.repositories;


import com.ga.capstone.models.Course;
import com.ga.capstone.models.Enrollment;
import com.ga.capstone.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Enrollment entity CRUD and custom queries.
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /**
     * Find all enrollments for a specific student.
     *
     * @param student the student user
     * @return list of enrollments
     */
    List<Enrollment> findByStudent(User student);

    /**
     * Find all enrollments for a specific course.
     *
     * @param course the course
     * @return list of enrollments
     */
    List<Enrollment> findByCourse(Course course);

    /**
     * Check if a student is already enrolled in a course.
     *
     * @param student the student user
     * @param course  the course
     * @return true if already enrolled
     */
    boolean existsByStudentAndCourse(User student, Course course);
}
