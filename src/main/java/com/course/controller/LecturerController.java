package com.course.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.course.entity.Attendance;
import com.course.entity.ClassSchedule;
import com.course.entity.Course;
import com.course.entity.User;
import com.course.service.LecturerService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/lecturer")
// @PreAuthorize("hasRole('LECTURER')") // DISABLED FOR TESTING
public class LecturerController {

    private final LecturerService lecturerService;

    public LecturerController(LecturerService lecturerService) {
        this.lecturerService = lecturerService;
    }

    @GetMapping("/courses")
    public List<Course> getCourses(@RequestParam long lecturerId) {
        // TODO: After enabling security, get lecturerId from Authentication
        return lecturerService.getCoursesByLecturerId(lecturerId);
    }

    @GetMapping("/courses/{offeringId}/schedule")
    public List<ClassSchedule> getClassSchedules(
            @PathVariable long offeringId,
            @RequestParam long lecturerId) {
        // TODO: After enabling security, get lecturerId from Authentication
        return lecturerService.getClassSchedulesByLecturerId(offeringId, lecturerId);
    }

    @GetMapping("/courses/{offeringId}/students")
    public List<User> getEnrolledStudents(
            @PathVariable long offeringId,
            @RequestParam long lecturerId) {
        // TODO: After enabling security, get lecturerId from Authentication
        return lecturerService.getEnrolledStudents(offeringId, lecturerId);
    }

    @PostMapping("/attendance")
    public ResponseEntity<String> recordAttendance(
            @RequestBody com.course.dto.attendance.AttendanceRequestDTO attendanceRequestDTO,
            @RequestParam long studentId,
            @RequestParam String status) {
        // TODO: After enabling security, validate lecturerId from Authentication
        lecturerService.recordAttendance(attendanceRequestDTO, studentId, status);
        return ResponseEntity.ok("Attendance recorded successfully.");
    }

    // --- Course offering CRUD for lecturers ---
    @PostMapping("/offerings")
    public ResponseEntity<?> createOffering(
            @RequestBody com.course.dto.course.CourseOfferingRequestDTO dto,
            @RequestParam long lecturerId) {
        var offering = lecturerService.createCourseOffering(lecturerId, dto);
        return ResponseEntity.ok(offering);
    }

    @PutMapping("/offerings/{id}")
    public ResponseEntity<?> updateOffering(
            @PathVariable("id") long id,
            @RequestBody com.course.dto.course.CourseOfferingRequestDTO dto,
            @RequestParam long lecturerId) {
        var offering = lecturerService.updateCourseOffering(lecturerId, id, dto);
        return ResponseEntity.ok(offering);
    }

    @DeleteMapping("/offerings/{id}")
    public ResponseEntity<?> deleteOffering(@PathVariable("id") long id, @RequestParam long lecturerId) {
        lecturerService.deleteCourseOffering(lecturerId, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/attendance/{scheduleId}")
    public List<Attendance> getAttendanceRecords(
            @PathVariable long scheduleId,
            @RequestParam long lecturerId) {
        // TODO: After enabling security, get lecturerId from Authentication
        return lecturerService.getAttendanceRecords(scheduleId, lecturerId);
    }

    // Attendance counts by date (last N days) for lecturer's offerings
    @GetMapping("/attendance/trends")
    public ResponseEntity<java.util.Map<String, Long>> getAttendanceTrends(
            @RequestParam long lecturerId,
            @RequestParam(required = false, defaultValue = "14") int days) {
        var map = lecturerService.getAttendanceCountsByDate(lecturerId, days);
        return ResponseEntity.ok(map);
    }

    // Course average grades for lecturer
    @GetMapping("/courses/averages")
    public ResponseEntity<java.util.Map<String, Double>> getCourseAverages(@RequestParam long lecturerId) {
        var map = lecturerService.getCourseAverageGradeByLecturer(lecturerId);
        return ResponseEntity.ok(map);
    }

}
