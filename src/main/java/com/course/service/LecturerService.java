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

        // Update an existing attendance record (only allowed for lecturer who owns the
        // offering)
        com.course.entity.Attendance updateAttendance(long attendanceId, AttendanceRequestDTO dto, Long lecturerId);

        // Delete an attendance record (only allowed for lecturer who owns the offering)
        void deleteAttendance(long attendanceId, Long lecturerId);

        List<Attendance> getAttendanceRecords(long scheduleId, Long lecturerId);

        java.util.List<java.util.Map<String, Object>> getAttendanceRecordsAsDto(long scheduleId, Long lecturerId);

        // Attendance trends for a lecturer (counts per date)
        java.util.Map<String, Long> getAttendanceCountsByDate(long lecturerId, int days);

        // Attendance trends (date range + optional course + optional enrollment status)
        java.util.Map<String, Long> getAttendanceCountsByDateRange(long lecturerId,
                        java.time.LocalDate from,
                        java.time.LocalDate to,
                        Long offeringId,
                        String studentStatus);

        // Reports
        double calculatePassRate(long lecturerId, long offeringId, String studentStatus);

        double calculateAverageAttendance(long lecturerId,
                        java.time.LocalDate from,
                        java.time.LocalDate to,
                        Long offeringId,
                        String studentStatus);

        java.util.List<com.course.dto.lecturer.LecturerCourseReportDTO> getCourseReports(long lecturerId,
                        java.time.LocalDate from,
                        java.time.LocalDate to,
                        String studentStatus);

        com.course.dto.lecturer.LecturerCourseDetailDTO getDetailedCourseReport(long lecturerId,
                        long offeringId,
                        java.time.LocalDate from,
                        java.time.LocalDate to,
                        String studentStatus);

        // Course performance (average numeric grade) for courses taught by lecturer
        java.util.Map<String, Double> getCourseAverageGradeByLecturer(long lecturerId);

        // Course offering CRUD
        com.course.entity.CourseOffering createCourseOffering(long lecturerId,
                        com.course.dto.course.CourseOfferingRequestDTO dto);

        com.course.entity.CourseOffering updateCourseOffering(long lecturerId, long offeringId,
                        com.course.dto.course.CourseOfferingRequestDTO dto);

        void deleteCourseOffering(long lecturerId, long offeringId);

        java.util.List<com.course.entity.CourseOffering> getOfferingsByLecturerId(long lecturerId);

        com.course.entity.CourseOffering getOfferingById(long lecturerId, long offeringId);

        com.course.entity.CourseOffering regenerateOfferingEnrollmentCode(long lecturerId, long offeringId);

}
