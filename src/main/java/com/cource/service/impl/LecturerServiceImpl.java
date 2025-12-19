package com.cource.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cource.dto.attendance.AttendanceRequestDTO;
import com.cource.entity.Attendance;
import com.cource.entity.ClassSchedule;
import com.cource.entity.Course;
import com.cource.entity.CourseLecturer;
import com.cource.entity.Enrollment;
import com.cource.entity.User;
import com.cource.repository.AttendanceRepository;
import com.cource.repository.ClassScheduleRepository;
import com.cource.repository.CourseLecturerRepository;
import com.cource.repository.CourseRepository;
import com.cource.repository.EnrollmentRepository;
import com.cource.service.LecturerService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class LecturerServiceImpl implements LecturerService {

    private final CourseLecturerRepository courseLecturerRepository;
    private final AttendanceRepository attendanceRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final EnrollmentRepository enrollmentRepository;

    public LecturerServiceImpl(CourseLecturerRepository courseLecturerRepository,
            AttendanceRepository attendanceRepository,
            ClassScheduleRepository classScheduleRepository,
            EnrollmentRepository enrollmentRepository) {
        this.courseLecturerRepository = courseLecturerRepository;
        this.attendanceRepository = attendanceRepository;
        this.classScheduleRepository = classScheduleRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public List<Course> getCoursesByLecturerId(long lecturerId) {
        return courseLecturerRepository.findCoursesByLecturerId(lecturerId).stream().map(CourseLecturer::getCourse)
                .toList();
    }

    @Override
    public List<ClassSchedule> getClassSchedulesByLecturerId(long coursesId, long lecturerId) {
        verifyOwnership(coursesId, lecturerId);
        return classScheduleRepository.findByCourseIdAndLecturerId(coursesId, lecturerId);
    }

    @Override
    public List<User> getEnrolledStudents(long courseId, long lecturerId) {
        verifyOwnership(courseId, lecturerId);
        return enrollmentRepository.findEnrolledStudentsByCourseId(courseId).stream().map(Enrollment::getStudent)
                .toList();
    }

    @Override
    public void recordAttendance(AttendanceRequestDTO attendanceRequestDTO, long studentId, String status) {
        verifyOwnership(
                classScheduleRepository.findCourseIdByScheduleId(attendanceRequestDTO.getScheduleId()).orElseThrow()
                        .getCourse().getId(),
                attendanceRequestDTO.getLecturerId());

        Attendance attendance = new Attendance();
        attendance.setEnrollmentId(enrollmentRepository.findByStudentIdAndCourseId(studentId,
                classScheduleRepository.findCourseIdByScheduleId(attendanceRequestDTO.getScheduleId()).orElseThrow()
                        .getCourse().getId())
                .orElseThrow().getId());
        attendance.setAttendanceDate(attendanceRequestDTO.getAttendanceDate());
        attendance.setScheduleId(attendanceRequestDTO.getScheduleId());
        attendance.setStudentId(studentId);
        attendance.setStatus(status);
        attendance.setRecordedBy(attendanceRequestDTO.getLecturerId());
        attendanceRepository.save(attendance);

    }

    @Override
    public List<Attendance> getAttendanceRecords(long scheduleId, long lecturerId) {
        verifyOwnership(
                classScheduleRepository.findCourseIdByScheduleId(scheduleId).orElseThrow().getCourse().getId(),
                lecturerId);
        return attendanceRepository.findByScheduleId(scheduleId);
    }

    private void verifyOwnership(long courseId, long lecturerId) {
        boolean isOwner = courseLecturerRepository.existsByCourseIdAndLecturerId(courseId, lecturerId);
        if (!isOwner) {
            throw new SecurityException("Lecturer does not have access to this course.");
        }
    }

}
