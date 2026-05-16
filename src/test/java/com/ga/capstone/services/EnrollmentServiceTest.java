package com.ga.capstone.services;


import com.ga.capstone.enums.EnrollmentStatus;
import com.ga.capstone.exceptions.ResourceAlreadyExistsException;
import com.ga.capstone.exceptions.ResourceNotFoundException;
import com.ga.capstone.models.Course;
import com.ga.capstone.models.Enrollment;
import com.ga.capstone.models.User;
import com.ga.capstone.repositories.CourseRepository;
import com.ga.capstone.repositories.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    // ============ TEST DATA ============
    private User student;
    private Course course;
    private Enrollment enrollment1;

    // ============ SETUP ============
    @BeforeEach
    void setUp() {
        student = new User();
        student.setId(1L);
        student.setEmail("student@test.com");

        course = new Course();
        course.setId(1L);
        course.setTitle("Java Basics");
        course.setDeleted(false);

        enrollment1 = new Enrollment();
        enrollment1.setId(1L);
        enrollment1.setStudent(student);
        enrollment1.setCourse(course);
        enrollment1.setStatus(EnrollmentStatus.ACTIVE);
    }

    // ============ TEST: enrollStudent ============
    @Test
    @DisplayName("enrollStudent: valid enrollment → saves and returns enrollment")
    void enrollStudent_validEnrollment_savesAndReturnsEnrollment() {
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentAndCourse(student, course)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment1);

        Enrollment result = enrollmentService.enrollStudent(1L, student);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);

        verify(courseRepository, times(1)).findByIdAndDeletedFalse(1L);
        verify(enrollmentRepository, times(1)).existsByStudentAndCourse(student, course);
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("enrollStudent: already enrolled → throws ResourceAlreadyExistsException")
    void enrollStudent_alreadyEnrolled_throwsResourceAlreadyExistsException() {
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentAndCourse(student, course)).thenReturn(true);

        assertThatThrownBy(() -> enrollmentService.enrollStudent(1L, student))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("already enrolled");

        verify(enrollmentRepository, times(1)).existsByStudentAndCourse(student, course);
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("enrollStudent: race condition (unique constraint) → throws ResourceAlreadyExistsException")
    void enrollStudent_raceCondition_throwsResourceAlreadyExistsException() {
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentAndCourse(student, course)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenThrow(new DataIntegrityViolationException("Unique constraint violated"));

        assertThatThrownBy(() -> enrollmentService.enrollStudent(1L, student))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("enrollStudent: course not found → throws ResourceNotFoundException")
    void enrollStudent_courseNotFound_throwsResourceNotFoundException() {
        when(courseRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.enrollStudent(99L, student))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(courseRepository, times(1)).findByIdAndDeletedFalse(99L);
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    // ============ TEST: getMyEnrollments ============
    @Test
    @DisplayName("getMyEnrollments: returns student's enrollments")
    void getMyEnrollments_returnsStudentEnrollments() {
        when(enrollmentRepository.findByStudent(student)).thenReturn(Arrays.asList(enrollment1));

        List<Enrollment> result = enrollmentService.getMyEnrollments(student);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(enrollment1);

        verify(enrollmentRepository, times(1)).findByStudent(student);
    }

    // ============ TEST: dropEnrollment ============
    @Test
    @DisplayName("dropEnrollment: valid owner → marks as DROPPED")
    void dropEnrollment_validOwner_marksAsDropped() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment1));

        enrollmentService.dropEnrollment(1L, student);

        assertThat(enrollment1.getStatus()).isEqualTo(EnrollmentStatus.DROPPED);

        verify(enrollmentRepository, times(1)).findById(1L);
        verify(enrollmentRepository, times(1)).save(enrollment1);
    }

    @Test
    @DisplayName("dropEnrollment: not owner → throws ResourceNotFoundException")
    void dropEnrollment_notOwner_throwsResourceNotFoundException() {
        User otherStudent = new User();
        otherStudent.setId(99L);

        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment1));

        assertThatThrownBy(() -> enrollmentService.dropEnrollment(1L, otherStudent))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(enrollmentRepository, times(1)).findById(1L);
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    // ============ TEST: updateEnrollmentStatus ============
    @Test
    @DisplayName("updateEnrollmentStatus: valid update → changes status")
    void updateEnrollmentStatus_validUpdate_changesStatus() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment1));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(i -> i.getArgument(0));

        Enrollment result = enrollmentService.updateEnrollmentStatus(1L, EnrollmentStatus.COMPLETED);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.COMPLETED);

        verify(enrollmentRepository, times(1)).findById(1L);
        verify(enrollmentRepository, times(1)).save(enrollment1);
    }
}
