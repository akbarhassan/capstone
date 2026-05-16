package com.ga.capstone.controllers;


import com.ga.capstone.enums.EnrollmentStatus;
import com.ga.capstone.models.Enrollment;
import com.ga.capstone.models.User;
import com.ga.capstone.response.SuccessResponse;
import com.ga.capstone.security.MyUserDetails;
import com.ga.capstone.services.EnrollmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class EnrollmentControllerTest {

    @Mock
    private EnrollmentService enrollmentService;

    @InjectMocks
    private EnrollmentController enrollmentController;

    // ============ TEST DATA ============
    private User student;
    private MyUserDetails userDetails;
    private Enrollment enrollment1;
    private Enrollment enrollment2;

    // ============ SETUP ============
    @BeforeEach
    void setUp() {
        student = new User();
        student.setId(1L);
        student.setEmail("student@test.com");

        userDetails = new MyUserDetails(student);

        enrollment1 = new Enrollment();
        enrollment1.setId(1L);
        enrollment1.setStatus(EnrollmentStatus.ACTIVE);
        enrollment1.setStudent(student);

        enrollment2 = new Enrollment();
        enrollment2.setId(2L);
        enrollment2.setStatus(EnrollmentStatus.ACTIVE);
    }

    // ============ TEST: enroll ============
    @Test
    @DisplayName("enroll: valid course → returns CREATED with enrollment data")
    void enroll_validCourse_returnsCreated() {
        when(enrollmentService.enrollStudent(eq(1L), any(User.class))).thenReturn(enrollment1);

        ResponseEntity<SuccessResponse> result = enrollmentController.enroll(1L, userDetails);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Enrolled successfully");
        assertThat(result.getBody().data()).isEqualTo(enrollment1);

        verify(enrollmentService, times(1)).enrollStudent(1L, student);
    }

    // ============ TEST: getMyEnrollments ============
    @Test
    @DisplayName("getMyEnrollments: returns student's enrollments")
    void getMyEnrollments_returnsStudentEnrollments() {
        List<Enrollment> enrollments = Arrays.asList(enrollment1);
        when(enrollmentService.getMyEnrollments(student)).thenReturn(enrollments);

        ResponseEntity<SuccessResponse> result = enrollmentController.getMyEnrollments(userDetails);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Enrollments retrieved successfully");
        assertThat(result.getBody().data()).isEqualTo(enrollments);

        verify(enrollmentService, times(1)).getMyEnrollments(student);
    }

    // ============ TEST: getAllEnrollments ============
    @Test
    @DisplayName("getAllEnrollments: returns all enrollments (admin)")
    void getAllEnrollments_returnsAllEnrollments() {
        List<Enrollment> enrollments = Arrays.asList(enrollment1, enrollment2);
        when(enrollmentService.getAllEnrollments()).thenReturn(enrollments);

        ResponseEntity<SuccessResponse> result = enrollmentController.getAllEnrollments();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("All enrollments retrieved successfully");
        assertThat(result.getBody().data()).isEqualTo(enrollments);

        verify(enrollmentService, times(1)).getAllEnrollments();
    }

    // ============ TEST: dropEnrollment ============
    @Test
    @DisplayName("dropEnrollment: drops enrollment and returns success")
    void dropEnrollment_dropsEnrollment() {
        doNothing().when(enrollmentService).dropEnrollment(1L, student);

        ResponseEntity<SuccessResponse> result = enrollmentController.dropEnrollment(1L, userDetails);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Enrollment dropped successfully");
        assertThat(result.getBody().data()).isNull();

        verify(enrollmentService, times(1)).dropEnrollment(1L, student);
    }
}
