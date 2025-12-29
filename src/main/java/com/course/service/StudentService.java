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

    // All terms (including past/future) for dropdowns
    List<AcademicTerm> getAllTerms();

    // Offerings available to a student (not already enrolled)
    java.util.List<com.course.entity.CourseOffering> getAvailableOfferings(Long studentId, Long termId, String keyword);

    // Enroll student into offering (with enrollment code)
    com.course.entity.Enrollment enrollInOffering(Long studentId, Long offeringId, String enrollmentCode);

    // Academic summaries
    double calculateGPA(Long studentId);

    int getCreditsEarned(Long studentId);

    int getCoursesCompleted(Long studentId);
}
