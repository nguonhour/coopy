package com.course.service;

import com.course.entity.*;
import java.util.List;

public interface StudentService {
    // Enrollment management
    List<Enrollment> getMyEnrollments(Long studentId);

    List<Enrollment> getMyGrades(Long studentId);

    // Schedule
    List<ClassSchedule> getMySchedule(Long studentId);

    // Attendance
    List<Attendance> getMyAttendance(Long studentId, Long offeringId);

    double getAttendancePercentage(Long studentId, Long offeringId);

    // Waitlist
    List<Waitlist> getMyWaitlistEntries(Long studentId);

    // Terms
    List<AcademicTerm> getActiveTerms();
}
