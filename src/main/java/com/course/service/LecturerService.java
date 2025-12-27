package com.course.service;

import java.util.List;

import com.course.dto.attendance.AttendanceRequestDTO;
import com.course.entity.Attendance;
import com.course.entity.ClassSchedule;
import com.course.entity.Course;
import com.course.entity.User;

public interface LecturerService {
    List<Course> getCoursesByLecturerId(long lecturerId);

    List<ClassSchedule> getClassSchedulesByLecturerId(long offeringId, long lecturerId);

    List<User> getEnrolledStudents(long offeringId, long lecturerId);

    void recordAttendance(AttendanceRequestDTO attendanceRequestDTO, long studentId, String status);

    List<Attendance> getAttendanceRecords(long scheduleId, long lecturerId);

    // Attendance trends for a lecturer (counts per date)
    java.util.Map<String, Long> getAttendanceCountsByDate(long lecturerId, int days);

    // Course performance (average numeric grade) for courses taught by lecturer
    java.util.Map<String, Double> getCourseAverageGradeByLecturer(long lecturerId);

    // Course offering CRUD
    com.course.entity.CourseOffering createCourseOffering(long lecturerId,
            com.course.dto.course.CourseOfferingRequestDTO dto);

    com.course.entity.CourseOffering updateCourseOffering(long lecturerId, long offeringId,
            com.course.dto.course.CourseOfferingRequestDTO dto);

    void deleteCourseOffering(long lecturerId, long offeringId);

    java.util.List<com.course.entity.CourseOffering> getOfferingsByLecturerId(long lecturerId);

}
