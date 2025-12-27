package com.course.service;

import com.course.dto.course.CourseCreateRequest;
import com.course.dto.course.CourseUpdateRequest;
import com.course.entity.Course;
import com.course.entity.User;

import java.util.List;

public interface CourseService {
    List<Course> getAllCourses();

    Course getCourseById(Long id);

    Course createCourse(CourseCreateRequest request);

    Course updateCourse(Long id, CourseUpdateRequest request);

    void deleteCourse(Long id);

    Course toggleCourseStatus(Long id);

    List<Course> searchCourses(String keyword);

    String generateEnrollmentCode(String courseCode);

    Course regenerateEnrollmentCode(Long id);

    // Direct lecturer assignment
    void assignLecturersToCourse(Long courseId, List<Long> lecturerIds);

    List<User> getLecturersForCourse(Long courseId);
}
