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
    private final com.course.repository.CourseOfferingRepository courseOfferingRepository;

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
    public java.util.List<com.course.entity.CourseOffering> getAvailableOfferings(Long studentId, Long termId,
            String keyword) {
        java.util.List<com.course.entity.CourseOffering> offerings;
        if (keyword != null && !keyword.isBlank() && termId != null) {
            offerings = courseOfferingRepository.findByCourseTitleContainingIgnoreCaseAndTermId(keyword, termId);
        } else if (keyword != null && !keyword.isBlank()) {
            offerings = courseOfferingRepository.findByCourseTitleContainingIgnoreCase(keyword);
        } else if (termId != null) {
            offerings = courseOfferingRepository.findActiveByTermId(termId);
        } else {
            // default: all active offerings
            offerings = courseOfferingRepository.findByActive(true);
        }

        // filter out offerings where student already enrolled
        java.util.List<com.course.entity.CourseOffering> out = new java.util.ArrayList<>();
        for (var off : offerings) {
            var exists = enrollmentRepository.findByStudentIdAndOfferingId(studentId, off.getId()).isPresent();
            if (!exists) {
                out.add(off);
            }
        }
        return out;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public com.course.entity.Enrollment enrollInOffering(Long studentId, Long offeringId) {
        var off = courseOfferingRepository.findById(offeringId)
                .orElseThrow(() -> new com.course.exception.ResourceNotFoundException("Offering not found"));
        var already = enrollmentRepository.findByStudentIdAndOfferingId(studentId, offeringId);
        if (already.isPresent()) {
            throw new IllegalArgumentException("Student already enrolled");
        }
        long enrolledCount = courseOfferingRepository.countEnrolledStudents(offeringId);
        if (enrolledCount >= off.getCapacity()) {
            throw new IllegalStateException("Offering is full");
        }
        com.course.entity.User student = new com.course.entity.User();
        student.setId(studentId);
        com.course.entity.Enrollment e = new com.course.entity.Enrollment();
        e.setStudent(student);
        e.setOffering(off);
        e.setStatus("ENROLLED");
        return enrollmentRepository.save(e);
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

    @Override
    public List<AcademicTerm> getAllTerms() {
        return academicTermRepository.findAll();
    }

    @Override
    public double calculateGPA(Long studentId) {
        List<Enrollment> grades = getMyGrades(studentId);
        if (grades == null || grades.isEmpty())
            return 0.0;

        double totalPoints = 0.0;
        int totalCredits = 0;
        for (var e : grades) {
            String g = e.getGrade();
            if (g == null)
                continue;
            int credits = 0;
            try {
                credits = e.getOffering().getCourse().getCredits();
            } catch (Exception ex) {
                credits = 0;
            }
            double pts = switch (g.toUpperCase()) {
                case "A+", "A" -> 4.0;
                case "A-" -> 3.7;
                case "B+" -> 3.3;
                case "B" -> 3.0;
                case "B-" -> 2.7;
                case "C+" -> 2.3;
                case "C" -> 2.0;
                case "C-" -> 1.7;
                case "D+" -> 1.3;
                case "D" -> 1.0;
                case "F", "W", "I" -> 0.0;
                default -> 0.0;
            };
            totalPoints += pts * credits;
            totalCredits += credits;
        }
        if (totalCredits == 0)
            return 0.0;
        return Math.round((totalPoints / totalCredits) * 100.0) / 100.0; // round to 2 decimals
    }

    @Override
    public int getCreditsEarned(Long studentId) {
        List<Enrollment> grades = getMyGrades(studentId);
        if (grades == null || grades.isEmpty())
            return 0;
        int sum = 0;
        for (var e : grades) {
            String g = e.getGrade();
            if (g == null)
                continue;
            if (g.equalsIgnoreCase("F") || g.equalsIgnoreCase("W") || g.equalsIgnoreCase("I"))
                continue;
            try {
                sum += e.getOffering().getCourse().getCredits();
            } catch (Exception ex) {
            }
        }
        return sum;
    }

    @Override
    public int getCoursesCompleted(Long studentId) {
        List<Enrollment> grades = getMyGrades(studentId);
        if (grades == null || grades.isEmpty())
            return 0;
        int count = 0;
        for (var e : grades) {
            String g = e.getGrade();
            if (g == null)
                continue;
            if (g.equalsIgnoreCase("F") || g.equalsIgnoreCase("W") || g.equalsIgnoreCase("I"))
                continue;
            count++;
        }
        return count;
    }
}
