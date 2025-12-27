
package com.course.service.impl;

import com.course.repository.CourseOfferingRepository;
import com.course.repository.EnrollmentRepository;
import com.course.repository.CourseLecturerRepository;
import com.course.repository.ClassScheduleRepository;
import com.course.entity.CourseOffering;
import com.course.entity.Enrollment;
import com.course.entity.User;
import com.course.entity.CourseLecturer;
import com.course.entity.ClassSchedule;

import com.course.dto.course.CourseCreateRequest;
import com.course.dto.course.CourseUpdateRequest;
import com.course.entity.Course;
import com.course.exception.ResourceNotFoundException;
import com.course.repository.CourseRepository;
import com.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseOfferingRepository courseOfferingRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseLecturerRepository courseLecturerRepository;
    private final ClassScheduleRepository classScheduleRepository;

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    @Override
    @Transactional
    public Course createCourse(CourseCreateRequest request) {
        Course course = new Course();
        course.setCourseCode(request.getCourseCode());
        course.setEnrollmentCode(generateEnrollmentCode(request.getCourseCode()));
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setCredits(request.getCredits());
        course.setActive(request.isActive());
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public Course updateCourse(Long id, CourseUpdateRequest request) {
        Course course = getCourseById(id);

        if (request.getCourseCode() != null) {
            course.setCourseCode(request.getCourseCode());
        }
        if (request.getTitle() != null) {
            course.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        if (request.getCredits() != null) {
            course.setCredits(request.getCredits());
        }
        if (request.getActive() != null) {
            course.setActive(request.getActive());
        }

        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = getCourseById(id);
        // Find all offerings for this course
        List<CourseOffering> offerings = courseOfferingRepository.findByCourseId(id);
        for (CourseOffering offering : offerings) {
            Long offeringId = offering.getId();
            // Delete all enrollments for this offering
            List<Enrollment> enrollments = enrollmentRepository.findByOfferingId(offeringId);
            enrollmentRepository.deleteAll(enrollments);
            // Delete all lecturer assignments for this offering
            List<CourseLecturer> lecturers = offering.getLecturers();
            courseLecturerRepository.deleteAll(lecturers);
            // Delete all class schedules for this offering
            List<ClassSchedule> schedules = classScheduleRepository.findByOfferingId(offeringId);
            classScheduleRepository.deleteAll(schedules);
        }
        // Delete all offerings for this course
        courseOfferingRepository.deleteAll(offerings);
        // Now delete the course
        courseRepository.delete(course);
    }

    @Override
    @Transactional
    public Course toggleCourseStatus(Long id) {
        Course course = getCourseById(id);
        course.setActive(!course.isActive());
        return courseRepository.save(course);
    }

    @Override
    public List<Course> searchCourses(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCourses();
        }
        return courseRepository.findByCourseCodeContainingIgnoreCaseOrTitleContainingIgnoreCase(keyword, keyword);
    }

    @Override
    public String generateEnrollmentCode(String courseCode) {
        String prefix = courseCode.length() >= 3 ? courseCode.substring(0, 3).toUpperCase() : courseCode.toUpperCase();
        Random random = new Random();
        int number = random.nextInt(10000);
        return String.format("%s%04d", prefix, number);
    }

    @Override
    @Transactional
    public Course regenerateEnrollmentCode(Long id) {
        Course course = getCourseById(id);
        course.setEnrollmentCode(generateEnrollmentCode(course.getCourseCode()));
        return courseRepository.save(course);
    }

    @Override
    public void assignLecturersToCourse(Long courseId, List<Long> lecturerIds) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'assignLecturersToCourse'");
    }

    @Override
    public List<User> getLecturersForCourse(Long courseId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLecturersForCourse'");
    }
}
