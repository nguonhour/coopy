package com.course.service.impl;

import com.course.entity.*;
import com.course.repository.*;
import com.course.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentServiceImpl implements StudentService {

    private final EnrollmentRepository enrollmentRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final AttendanceRepository attendanceRepository;
    private final WaitlistRepository waitlistRepository;
    private final AcademicTermRepository academicTermRepository;

    @Override
    public List<Enrollment> getMyEnrollments(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    @Override
    public List<Enrollment> getMyGrades(Long studentId) {
        return enrollmentRepository.findByStudentIdAndGradeIsNotNull(studentId);
    }

    @Override
    public List<ClassSchedule> getMySchedule(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        return enrollments.stream()
                .flatMap(e -> classScheduleRepository.findByOfferingId(e.getOffering().getId()).stream())
                .distinct()
                .toList();
    }

    @Override
    public List<Attendance> getMyAttendance(Long studentId, Long offeringId) {
        return attendanceRepository.findByStudentIdAndOfferingId(studentId, offeringId);
    }

    @Override
    public double getAttendancePercentage(Long studentId, Long offeringId) {
        List<Attendance> attendance = attendanceRepository.findByStudentIdAndOfferingId(studentId,
                offeringId);
        if (attendance.isEmpty()) {
            return 0.0;
        }
        long presentCount = attendance.stream().filter(a -> "PRESENT".equals(a.getStatus())).count();
        return (double) presentCount / attendance.size() * 100;
    }

    @Override
    public List<Waitlist> getMyWaitlistEntries(Long studentId) {
        return waitlistRepository.findByStudentIdOrderByPositionAsc(studentId);
    }

    @Override
    public List<AcademicTerm> getActiveTerms() {
        LocalDate now = LocalDate.now();
        return academicTermRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(now, now);
    }
}
