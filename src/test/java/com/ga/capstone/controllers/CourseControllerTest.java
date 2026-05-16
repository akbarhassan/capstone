package com.ga.capstone.controllers;


import com.ga.capstone.models.Course;
import com.ga.capstone.models.User;
import com.ga.capstone.response.SuccessResponse;
import com.ga.capstone.security.MyUserDetails;
import com.ga.capstone.services.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CourseControllerTest {

    @Mock
    private CourseService courseService;

    @InjectMocks
    private CourseController courseController;

    // ============ TEST DATA ============
    private User instructor;
    private MyUserDetails userDetails;
    private Course course1;
    private Course course2;

    // ============ SETUP ============
    @BeforeEach
    void setUp() {
        instructor = new User();
        instructor.setId(1L);
        instructor.setEmail("instructor@test.com");

        userDetails = new MyUserDetails(instructor);

        course1 = new Course();
        course1.setId(1L);
        course1.setTitle("Java Basics");
        course1.setDescription("Learn Java");

        course2 = new Course();
        course2.setId(2L);
        course2.setTitle("Spring Boot");
        course2.setDescription("Learn Spring");
    }

    // ============ TEST: createCourse ============
    @Test
    @DisplayName("createCourse: valid course → returns CREATED with course data")
    void createCourse_validCourse_returnsCreated() {
        when(courseService.createCourse(any(Course.class), eq(1L), eq(1L))).thenReturn(course1);

        ResponseEntity<SuccessResponse> result = courseController.createCourse(course1, 1L, userDetails);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Course created successfully");
        assertThat(result.getBody().data()).isEqualTo(course1);

        verify(courseService, times(1)).createCourse(course1, 1L, 1L);
    }

    // ============ TEST: getAllCourses ============
    @Test
    @DisplayName("getAllCourses: returns list of courses")
    void getAllCourses_returnsListOfCourses() {
        List<Course> courses = Arrays.asList(course1, course2);
        when(courseService.getAllCourses()).thenReturn(courses);

        ResponseEntity<SuccessResponse> result = courseController.getAllCourses();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Courses retrieved successfully");
        assertThat(result.getBody().data()).isEqualTo(courses);

        verify(courseService, times(1)).getAllCourses();
    }

    // ============ TEST: getCourseById ============
    @Test
    @DisplayName("getCourseById: returns course by ID")
    void getCourseById_returnsCourseById() {
        when(courseService.getCourseById(1L)).thenReturn(course1);

        ResponseEntity<SuccessResponse> result = courseController.getCourseById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Course retrieved successfully");
        assertThat(result.getBody().data()).isEqualTo(course1);

        verify(courseService, times(1)).getCourseById(1L);
    }

    // ============ TEST: updateCourse ============
    @Test
    @DisplayName("updateCourse: updates and returns course")
    void updateCourse_updatesAndReturnsCourse() {
        Course updateRequest = new Course();
        updateRequest.setTitle("Advanced Java");

        Course updatedCourse = new Course();
        updatedCourse.setId(1L);
        updatedCourse.setTitle("Advanced Java");

        when(courseService.updateCourse(eq(1L), any(Course.class))).thenReturn(updatedCourse);

        ResponseEntity<SuccessResponse> result = courseController.updateCourse(1L, updateRequest);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Course updated successfully");
        assertThat(result.getBody().data()).isEqualTo(updatedCourse);

        verify(courseService, times(1)).updateCourse(1L, updateRequest);
    }

    // ============ TEST: deleteCourse ============
    @Test
    @DisplayName("deleteCourse: soft deletes course and returns success")
    void deleteCourse_softDeletesCourse() {
        doNothing().when(courseService).softDeleteCourse(1L);

        ResponseEntity<SuccessResponse> result = courseController.deleteCourse(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Course deleted successfully");
        assertThat(result.getBody().data()).isNull();

        verify(courseService, times(1)).softDeleteCourse(1L);
    }

    // ============ TEST: uploadThumbnail ============
    @Test
    @DisplayName("uploadThumbnail: uploads thumbnail and returns updated course")
    void uploadThumbnail_uploadsThumbnail() {
        MockMultipartFile file = new MockMultipartFile("file", "thumb.png", "image/png", "fake".getBytes());

        Course updatedCourse = new Course();
        updatedCourse.setId(1L);
        updatedCourse.setThumbnail("1_12345.png");

        when(courseService.uploadCourseThumbnail(eq(1L), any())).thenReturn(updatedCourse);

        ResponseEntity<SuccessResponse> result = courseController.uploadThumbnail(1L, file);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Course thumbnail uploaded successfully");
        assertThat(result.getBody().data()).isEqualTo(updatedCourse);

        verify(courseService, times(1)).uploadCourseThumbnail(eq(1L), any());
    }

    // ============ TEST: getMyCourses ============
    @Test
    @DisplayName("getMyCourses: returns instructor's courses")
    void getMyCourses_returnsInstructorCourses() {
        when(courseService.getCoursesByInstructor(1L)).thenReturn(Arrays.asList(course1));

        ResponseEntity<SuccessResponse> result = courseController.getMyCourses(userDetails);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Courses retrieved successfully");

        verify(courseService, times(1)).getCoursesByInstructor(1L);
    }

    // ============ TEST: getCoursesByCategory ============
    @Test
    @DisplayName("getCoursesByCategory: returns courses for category")
    void getCoursesByCategory_returnsCourses() {
        when(courseService.getCoursesByCategory(1L)).thenReturn(Arrays.asList(course1));

        ResponseEntity<SuccessResponse> result = courseController.getCoursesByCategory(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Courses retrieved successfully");

        verify(courseService, times(1)).getCoursesByCategory(1L);
    }
}
