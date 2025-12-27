package com.course.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.course.dto.attendance.AttendanceRequestDTO;
import com.course.entity.Attendance;
import com.course.entity.ClassSchedule;
import com.course.entity.Course;
import com.course.entity.Enrollment;
import com.course.entity.User;
import com.course.exception.ResourceNotFoundException;
import com.course.repository.AttendanceRepository;
import com.course.repository.ClassScheduleRepository;
import com.course.repository.CourseLecturerRepository;
import com.course.repository.EnrollmentRepository;
import com.course.service.LecturerService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class LecturerServiceImpl implements LecturerService {

    private final CourseLecturerRepository courseLecturerRepository;
    private final AttendanceRepository attendanceRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final com.course.repository.CourseOfferingRepository courseOfferingRepository;
    private final com.course.repository.CourseRepository courseRepository;
    private final com.course.repository.AcademicTermRepository academicTermRepository;

    public LecturerServiceImpl(CourseLecturerRepository courseLecturerRepository,
            AttendanceRepository attendanceRepository,
            ClassScheduleRepository classScheduleRepository,
            EnrollmentRepository enrollmentRepository,
            com.course.repository.CourseOfferingRepository courseOfferingRepository,
            com.course.repository.CourseRepository courseRepository,
            com.course.repository.AcademicTermRepository academicTermRepository) {
        this.attendanceRepository = attendanceRepository;
        this.classScheduleRepository = classScheduleRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseLecturerRepository = courseLecturerRepository;
        this.courseOfferingRepository = courseOfferingRepository;
        this.courseRepository = courseRepository;
        this.academicTermRepository = academicTermRepository;
    }

    @Override
    public List<Course> getCoursesByLecturerId(long lecturerId) {
        return courseLecturerRepository.findByLecturerId(lecturerId).stream()
                .map(cl -> cl.getOffering().getCourse())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassSchedule> getClassSchedulesByLecturerId(long offeringId, long lecturerId) {
        verifyOwnership(offeringId, lecturerId);
        return classScheduleRepository.findByOfferingIdAndLecturerId(offeringId, lecturerId);
    }

    @Override
    public List<User> getEnrolledStudents(long offeringId, long lecturerId) {
        verifyOwnership(offeringId, lecturerId);
        List<User> students = enrollmentRepository.findByOfferingId(offeringId).stream()
                .filter(e -> "ENROLLED".equalsIgnoreCase(e.getStatus()))
                .map(Enrollment::getStudent)
                .collect(Collectors.toList());
        System.out.println("[DEBUG] getEnrolledStudents for offeringId=" + offeringId + ", lecturerId=" + lecturerId);
        for (User s : students) {
            System.out.println("[DEBUG] Student: id=" + s.getId() + ", email=" + s.getEmail() + ", firstName="
                    + s.getFirstName() + ", lastName=" + s.getLastName() + ", active=" + s.isActive() + ", role="
                    + (s.getRole() != null ? s.getRole().getRoleName() : "null"));
        }
        return students;
    }

    @Override
    public void recordAttendance(AttendanceRequestDTO attendanceRequestDTO, long studentId, String status) {
        ClassSchedule schedule = classScheduleRepository.findById(attendanceRequestDTO.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        verifyOwnership(schedule.getOffering().getId(), attendanceRequestDTO.getLecturerId());

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndOfferingId(
                studentId, schedule.getOffering().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        Attendance attendance = new Attendance();
        attendance.setEnrollment(enrollment);
        attendance.setSchedule(schedule);
        attendance.setAttendanceDate(attendanceRequestDTO.getAttendanceDate());
        attendance.setStatus(status);

        User lecturer = new User();
        lecturer.setId(attendanceRequestDTO.getLecturerId());
        attendance.setRecordedBy(lecturer);

        if (attendanceRequestDTO.getNotes() != null) {
            attendance.setNotes(attendanceRequestDTO.getNotes());
        }

        attendanceRepository.save(attendance);
    }

    @Override
    public List<Attendance> getAttendanceRecords(long scheduleId, long lecturerId) {
        ClassSchedule schedule = classScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        verifyOwnership(schedule.getOffering().getId(), lecturerId);
        return attendanceRepository.findByScheduleId(scheduleId);
    }

    @Override
    public java.util.Map<String, Long> getAttendanceCountsByDate(long lecturerId, int days) {
        var offerings = courseLecturerRepository.findByLecturerId(lecturerId).stream()
                .map(cl -> cl.getOffering().getId()).distinct().toList();

        java.util.Map<String, Long> result = new java.util.LinkedHashMap<>();
        if (offerings.isEmpty())
            return result;

        java.time.LocalDate from = java.time.LocalDate.now().minusDays(days - 1);
        var rows = attendanceRepository.countByOfferingIdsSince(offerings, from);
        for (Object[] r : rows) {
            java.time.LocalDate date = (java.time.LocalDate) r[0];
            Long count = ((Number) r[1]).longValue();
            result.put(date.toString(), count);
        }
        return result;
    }

    @Override
    public java.util.Map<String, Double> getCourseAverageGradeByLecturer(long lecturerId) {
        var offerings = courseLecturerRepository.findByLecturerId(lecturerId).stream()
                .map(cl -> cl.getOffering()).distinct().toList();
        java.util.Map<String, Double> out = new java.util.LinkedHashMap<>();
        for (var off : offerings) {
            var enrolls = enrollmentRepository.findByOfferingId(off.getId()).stream()
                    .filter(e -> e.getGrade() != null && !e.getGrade().isEmpty()).toList();
            if (enrolls.isEmpty()) {
                out.put(off.getCourse().getCourseCode() + " - " + off.getCourse().getTitle(), 0.0);
                continue;
            }
            double sum = 0.0;
            int count = 0;
            for (var e : enrolls) {
                String g = e.getGrade().toUpperCase();
                Double val = switch (g) {
                    case "A" -> 4.0;
                    case "B" -> 3.0;
                    case "C" -> 2.0;
                    case "D" -> 1.0;
                    case "F" -> 0.0;
                    default -> null;
                };
                if (val != null) {
                    sum += val;
                    count++;
                }
            }
            double avg = count == 0 ? 0.0 : sum / count;
            out.put(off.getCourse().getCourseCode() + " - " + off.getCourse().getTitle(), avg);
        }
        return out;
    }

    @Override
    public java.util.List<com.course.entity.CourseOffering> getOfferingsByLecturerId(long lecturerId) {
        var offerings = courseLecturerRepository.findByLecturerId(lecturerId).stream()
                .map(cl -> cl.getOffering())
                .distinct()
                .toList();
        System.out.println("[DEBUG] getOfferingsByLecturerId for lecturerId=" + lecturerId);
        for (var off : offerings) {
            System.out.println("[DEBUG] Offering: id=" + off.getId() + ", course="
                    + (off.getCourse() != null ? off.getCourse().getCourseCode() : "null") + ", term="
                    + (off.getTerm() != null ? off.getTerm().getTermCode() : "null"));
        }
        return offerings;
    }

    private void verifyOwnership(long offeringId, long lecturerId) {
        boolean isOwner = courseLecturerRepository.existsByOfferingIdAndLecturerId(offeringId, lecturerId);
        if (!isOwner) {
            throw new SecurityException("Lecturer does not have access to this course offering.");
        }
    }

    @Override
    public com.course.entity.CourseOffering createCourseOffering(long lecturerId,
            com.course.dto.course.CourseOfferingRequestDTO dto) {
        if (dto.getCourseId() == null || dto.getTermId() == null) {
            throw new IllegalArgumentException("courseId and termId are required");
        }
        var course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        var term = academicTermRepository.findById(dto.getTermId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic term not found"));

        // Check unique offering
        var existing = courseOfferingRepository.findByCourseIdAndTermId(course.getId(), term.getId());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("An offering for this course and term already exists");
        }

        com.course.entity.CourseOffering offering = new com.course.entity.CourseOffering();
        offering.setCourse(course);
        offering.setTerm(term);
        if (dto.getCapacity() != null)
            offering.setCapacity(dto.getCapacity());
        if (dto.getActive() != null)
            offering.setActive(dto.getActive());

        offering = courseOfferingRepository.save(offering);

        // assign lecturer as primary
        com.course.entity.User lecturer = new com.course.entity.User();
        lecturer.setId(lecturerId);
        com.course.entity.CourseLecturer cl = new com.course.entity.CourseLecturer();
        cl.setOffering(offering);
        cl.setLecturer(lecturer);
        cl.setPrimary(true);
        courseLecturerRepository.save(cl);

        return offering;
    }

    @Override
    public com.course.entity.CourseOffering updateCourseOffering(long lecturerId, long offeringId,
            com.course.dto.course.CourseOfferingRequestDTO dto) {
        verifyOwnership(offeringId, lecturerId);
        var offering = courseOfferingRepository.findById(offeringId)
                .orElseThrow(() -> new ResourceNotFoundException("Offering not found"));
        if (dto.getCourseId() != null
                && (offering.getCourse() == null || !offering.getCourse().getId().equals(dto.getCourseId()))) {
            var course = courseRepository.findById(dto.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            offering.setCourse(course);
        }
        if (dto.getTermId() != null
                && (offering.getTerm() == null || !offering.getTerm().getId().equals(dto.getTermId()))) {
            var term = academicTermRepository.findById(dto.getTermId())
                    .orElseThrow(() -> new ResourceNotFoundException("Term not found"));
            offering.setTerm(term);
        }
        if (dto.getCapacity() != null)
            offering.setCapacity(dto.getCapacity());
        if (dto.getActive() != null)
            offering.setActive(dto.getActive());
        return courseOfferingRepository.save(offering);
    }

    @Override
    public void deleteCourseOffering(long lecturerId, long offeringId) {
        verifyOwnership(offeringId, lecturerId);
        courseOfferingRepository.deleteById(offeringId);
    }

}
