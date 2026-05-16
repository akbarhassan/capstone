package com.ga.capstone.services;


import com.ga.capstone.enums.EnrollmentStatus;
import com.ga.capstone.exceptions.ResourceAlreadyExistsException;
import com.ga.capstone.exceptions.ResourceNotFoundException;
import com.ga.capstone.models.Course;
import com.ga.capstone.models.Enrollment;
import com.ga.capstone.models.User;
import com.ga.capstone.repositories.CourseRepository;
import com.ga.capstone.repositories.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service handling student enrollment operations:
 * enrolling, listing, dropping, and updating enrollment status.
 */
@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    /**
     * Enroll a student in a course.
     *
     * @param courseId the course ID
     * @param student the student user
     * @return the created enrollment
     * @throws ResourceAlreadyExistsException if already enrolled
     */
    @Transactional
    public Enrollment enrollStudent(Long courseId, User student) {
        Course course = courseRepository.findByIdAndDeletedFalse(courseId).orElseThrow(
                () -> new ResourceNotFoundException("Course not found with id: " + courseId)
        );

        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            throw new ResourceAlreadyExistsException("You are already enrolled in this course");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);

        return enrollmentRepository.save(enrollment);
    }

    /**
     * Get all enrollments for the current student.
     *
     * @param student the student user
     * @return list of enrollments
     */
    @Transactional(readOnly = true)
    public List<Enrollment> getMyEnrollments(User student) {
        return enrollmentRepository.findByStudent(student);
    }

    /**
     * Get all enrollments (admin view).
     *
     * @return list of all enrollments
     */
    @Transactional(readOnly = true)
    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    /**
     * Drop an enrollment (student withdraws from course).
     *
     * @param enrollmentId the enrollment ID
     * @param student      the student requesting the drop
     */
    @Transactional
    public void dropEnrollment(Long enrollmentId, User student) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(
                () -> new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId)
        );

        if (!enrollment.getStudent().getId().equals(student.getId())) {
            throw new ResourceNotFoundException("Enrollment not found for current user");
        }

        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);
    }

    /**
     * Update enrollment status (admin or system operation).
     *
     * @param enrollmentId the enrollment ID
     * @param status       the new status
     * @return the updated enrollment
     */
    @Transactional
    public Enrollment updateEnrollmentStatus(Long enrollmentId, EnrollmentStatus status) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(
                () -> new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId)
        );

        enrollment.setStatus(status);
        return enrollmentRepository.save(enrollment);
    }
}
