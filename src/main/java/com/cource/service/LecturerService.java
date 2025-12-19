package com.cource.service;

import java.util.List;

import com.cource.dto.attendance.AttendanceRequestDTO;
import com.cource.entity.Attendance;
import com.cource.entity.ClassSchedule;
import com.cource.entity.Course;
import com.cource.entity.User;

public interface LecturerService {
    List<Course> getCoursesByLecturerId(long lecturerId);

    List<ClassSchedule> getClassSchedulesByLecturerId(long coursesId, long lecturerId);

    List<User> getEnrolledStudents(long courseId, long lecturerId);

    void recordAttendance(AttendanceRequestDTO attendanceRequestDTO, long studentId, String status);

    List<Attendance> getAttendanceRecords(long scheduleId, long lecturerId);

}
