package com.ga.capstone.controllers;


import com.ga.capstone.models.Enrollment;
import com.ga.capstone.response.SuccessResponse;
import com.ga.capstone.security.MyUserDetails;
import com.ga.capstone.services.EnrollmentService;
import com.ga.capstone.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for enrollment operations: enroll, list, and drop.
 */
@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * Enroll the authenticated student in a course.
     *
     * @param courseId    the course ID to enroll in
     * @param userDetails the authenticated user
     * @return the created enrollment
     */
    @PostMapping("/{courseId}")
    @PreAuthorize("hasAuthority('enrollment:create')")
    public ResponseEntity<SuccessResponse> enroll(
            @PathVariable Long courseId,
            @AuthenticationPrincipal MyUserDetails userDetails) {
        Enrollment enrollment = enrollmentService.enrollStudent(courseId, userDetails.getUser());
        return ResponseBuilder.success(HttpStatus.CREATED, "Enrolled successfully", enrollment);
    }

    /**
     * Get all enrollments for the authenticated student.
     *
     * @param userDetails the authenticated user
     * @return list of enrollments
     */
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('enrollment:read')")
    public ResponseEntity<SuccessResponse> getMyEnrollments(@AuthenticationPrincipal MyUserDetails userDetails) {
        List<Enrollment> enrollments = enrollmentService.getMyEnrollments(userDetails.getUser());
        return ResponseBuilder.success(HttpStatus.OK, "Enrollments retrieved successfully", enrollments);
    }

    /**
     * Get all enrollments (admin view).
     *
     * @return list of all enrollments
     */
    @GetMapping
    @PreAuthorize("hasAuthority('enrollment:read')")
    public ResponseEntity<SuccessResponse> getAllEnrollments() {
        List<Enrollment> enrollments = enrollmentService.getAllEnrollments();
        return ResponseBuilder.success(HttpStatus.OK, "All enrollments retrieved successfully", enrollments);
    }

    /**
     * Drop an enrollment (student withdraws from course).
     *
     * @param id          the enrollment ID
     * @param userDetails the authenticated user
     * @return success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('enrollment:delete')")
    public ResponseEntity<SuccessResponse> dropEnrollment(
            @PathVariable Long id,
            @AuthenticationPrincipal MyUserDetails userDetails) {
        enrollmentService.dropEnrollment(id, userDetails.getUser());
        return ResponseBuilder.success(HttpStatus.OK, "Enrollment dropped successfully", null);
    }
}
