package com.ga.capstone.controllers;


import com.ga.capstone.models.Lesson;
import com.ga.capstone.response.SuccessResponse;
import com.ga.capstone.services.LessonService;
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
public class LessonControllerTest {

    @Mock
    private LessonService lessonService;

    @InjectMocks
    private LessonController lessonController;

    // ============ TEST DATA ============
    private Lesson lesson1;
    private Lesson lesson2;

    // ============ SETUP ============
    @BeforeEach
    void setUp() {
        lesson1 = new Lesson();
        lesson1.setId(1L);
        lesson1.setTitle("Introduction");
        lesson1.setContent("Welcome to the course");
        lesson1.setSortOrder(1);

        lesson2 = new Lesson();
        lesson2.setId(2L);
        lesson2.setTitle("Variables");
        lesson2.setContent("Learn about variables");
        lesson2.setSortOrder(2);
    }

    // ============ TEST: createLesson ============
    @Test
    @DisplayName("createLesson: valid lesson → returns CREATED with lesson data")
    void createLesson_validLesson_returnsCreated() {
        when(lessonService.createLesson(eq(1L), any(Lesson.class))).thenReturn(lesson1);

        ResponseEntity<SuccessResponse> result = lessonController.createLesson(1L, lesson1);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Lesson created successfully");
        assertThat(result.getBody().data()).isEqualTo(lesson1);

        verify(lessonService, times(1)).createLesson(1L, lesson1);
    }

    // ============ TEST: getLessonsByCourse ============
    @Test
    @DisplayName("getLessonsByCourse: returns list of lessons")
    void getLessonsByCourse_returnsListOfLessons() {
        List<Lesson> lessons = Arrays.asList(lesson1, lesson2);
        when(lessonService.getLessonsByCourse(1L)).thenReturn(lessons);

        ResponseEntity<SuccessResponse> result = lessonController.getLessonsByCourse(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Lessons retrieved successfully");
        assertThat(result.getBody().data()).isEqualTo(lessons);

        verify(lessonService, times(1)).getLessonsByCourse(1L);
    }

    // ============ TEST: getLessonById ============
    @Test
    @DisplayName("getLessonById: returns lesson by ID")
    void getLessonById_returnsLessonById() {
        when(lessonService.getLessonById(1L)).thenReturn(lesson1);

        ResponseEntity<SuccessResponse> result = lessonController.getLessonById(1L, 1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Lesson retrieved successfully");
        assertThat(result.getBody().data()).isEqualTo(lesson1);

        verify(lessonService, times(1)).getLessonById(1L);
    }

    // ============ TEST: updateLesson ============
    @Test
    @DisplayName("updateLesson: updates and returns lesson")
    void updateLesson_updatesAndReturnsLesson() {
        Lesson updateRequest = new Lesson();
        updateRequest.setTitle("Updated Lesson");

        Lesson updatedLesson = new Lesson();
        updatedLesson.setId(1L);
        updatedLesson.setTitle("Updated Lesson");

        when(lessonService.updateLesson(eq(1L), any(Lesson.class))).thenReturn(updatedLesson);

        ResponseEntity<SuccessResponse> result = lessonController.updateLesson(1L, 1L, updateRequest);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Lesson updated successfully");
        assertThat(result.getBody().data()).isEqualTo(updatedLesson);

        verify(lessonService, times(1)).updateLesson(1L, updateRequest);
    }

    // ============ TEST: deleteLesson ============
    @Test
    @DisplayName("deleteLesson: deletes lesson and returns success")
    void deleteLesson_deletesLesson() {
        doNothing().when(lessonService).deleteLesson(1L);

        ResponseEntity<SuccessResponse> result = lessonController.deleteLesson(1L, 1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().message()).isEqualTo("Lesson deleted successfully");
        assertThat(result.getBody().data()).isNull();

        verify(lessonService, times(1)).deleteLesson(1L);
    }
}
