package com.course.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.course.service.LecturerService;
import com.course.service.AdminService;
import com.course.repository.RoleRepository;
import com.course.repository.ClassScheduleRepository;
import java.util.Map;
import java.util.LinkedHashMap;
import com.course.entity.Enrollment;

/**
 * Controller for rendering Lecturer HTML views (Thymeleaf templates)
 * Separate from REST API endpoints in LecturerController
 */
@Controller
@RequestMapping("/lecturer")
public class LecturerViewController {

    private final LecturerService lecturerService;
    private final AdminService adminService;
    private final RoleRepository roleRepository;
    private final ClassScheduleRepository classScheduleRepository;

    public LecturerViewController(LecturerService lecturerService, AdminService adminService,
            RoleRepository roleRepository, ClassScheduleRepository classScheduleRepository) {
        this.lecturerService = lecturerService;
        this.adminService = adminService;
        this.roleRepository = roleRepository;
        this.classScheduleRepository = classScheduleRepository;
    }

    /**
     * Render lecturer dashboard page
     */
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) Long lecturerId, Model model) {
        // TODO: After enabling security, get lecturerId from Authentication
        if (lecturerId != null) {
            model.addAttribute("lecturerId", lecturerId);
            // You can fetch dashboard data here if needed
            // model.addAttribute("courses",
            // lecturerService.getCoursesByLecturerId(lecturerId));
        }

        // Dashboard totals for the lecturer: total courses, total students, classes
        // today
        if (lecturerId != null) {
            var offerings = lecturerService.getOfferingsByLecturerId(lecturerId);
            int totalCourses = offerings == null ? 0 : offerings.size();
            long totalStudents = 0;
            int classesToday = 0;
            var today = java.time.LocalDate.now();
            if (offerings != null) {
                for (var off : offerings) {
                    try {
                        var students = lecturerService.getEnrolledStudents(off.getId(), lecturerId);
                        if (students != null)
                            totalStudents += students.size();
                    } catch (Exception ignored) {
                    }
                    try {
                        var schedules = lecturerService.getClassSchedulesByLecturerId(off.getId(), lecturerId);
                        if (schedules != null) {
                            java.time.DayOfWeek dow = today.getDayOfWeek();
                            String dowFull = dow.name(); // e.g., MONDAY
                            String dowShort = dowFull.substring(0, 3); // e.g., MON
                            String dowShortDisplay = dow.getDisplayName(java.time.format.TextStyle.SHORT,
                                    java.util.Locale.ENGLISH).toUpperCase(); // e.g., Mon -> MON
                            for (var s : schedules) {
                                String schedDow = s.getDayOfWeek();
                                if (schedDow == null)
                                    continue;
                                String sd = schedDow.trim().toUpperCase();
                                if (sd.equals(dowFull) || sd.equals(dowShort) || sd.equals(dowShortDisplay)) {
                                    classesToday++;
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
            model.addAttribute("totalCourses", totalCourses);
            model.addAttribute("totalStudents", totalStudents);
            model.addAttribute("classesToday", classesToday);
        }
        // Provide chart data using LecturerService: recent attendance (7 days) + role
        // distribution
        model.addAttribute("systemTotalStudents", adminService.getTotalStudents());
        model.addAttribute("systemTotalLecturers", adminService.getTotalLecturers());

        if (lecturerId != null) {
            var attendanceMap = lecturerService.getAttendanceCountsByDate(lecturerId, 7);
            var enrollmentLabels = new java.util.ArrayList<String>(attendanceMap.keySet());
            var enrollmentData = new java.util.ArrayList<Number>();
            for (String k : enrollmentLabels) {
                enrollmentData.add(attendanceMap.getOrDefault(k, 0L));
            }
            model.addAttribute("enrollmentLabels", enrollmentLabels);
            model.addAttribute("enrollmentData", enrollmentData);

            var roles = roleRepository.findAll();
            var userLabels = new java.util.ArrayList<String>();
            var userData = new java.util.ArrayList<Number>();
            for (var r : roles) {
                long count = adminService.getUsersByRole(r.getRoleCode()).size();
                userLabels.add(r.getRoleName());
                userData.add(count);
            }
            model.addAttribute("userLabels", userLabels);
            model.addAttribute("userData", userData);
        } else {
            model.addAttribute("enrollmentLabels", new java.util.ArrayList<String>());
            model.addAttribute("enrollmentData", new java.util.ArrayList<Number>());
            model.addAttribute("userLabels", new java.util.ArrayList<String>());
            model.addAttribute("userData", new java.util.ArrayList<Number>());
        }
        return "views/lecturer/dashboard";
    }

    /**
     * Render courses page
     */
    @GetMapping("/courses")
    public String courses(@RequestParam(required = false) Long lecturerId, Model model) {
        // TODO: After enabling security, get lecturerId from Authentication
        try {
            if (lecturerId != null) {
                model.addAttribute("lecturerId", lecturerId);
                model.addAttribute("offerings", lecturerService.getOfferingsByLecturerId(lecturerId));
            }
            // Always provide all courses and all terms for the modal
            model.addAttribute("courses", adminService.getAllCourses());
            model.addAttribute("terms", adminService.getAllTerms());
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred");
        }
        return "views/lecturer/courses";
    }

    /**
     * Render students page
     */
    @GetMapping("/students")
    public String students(
            @RequestParam(required = false) Long offeringId,
            @RequestParam(required = false) Long lecturerId,
            Model model) {
        // TODO: After enabling security, get lecturerId from Authentication
        if (offeringId != null && lecturerId != null) {
            try {
                model.addAttribute("offeringId", offeringId);
                model.addAttribute("lecturerId", lecturerId);
                var enrollments = adminService.getEnrollmentsByOffering(offeringId);
                model.addAttribute("enrollments", enrollments);
                // Build student list for the template (template expects `students`)
                java.util.List<com.course.entity.User> students = new java.util.ArrayList<>();
                if (enrollments != null) {
                    for (Object o : enrollments) {
                        try {
                            com.course.entity.Enrollment e = (com.course.entity.Enrollment) o;
                            if (e != null && e.getStudent() != null) {
                                // only include students with ENROLLED status
                                if (e.getStatus() == null || "ENROLLED".equalsIgnoreCase(e.getStatus())) {
                                    students.add(e.getStudent());
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
                System.out.println("[DEBUG] LecturerViewController.students offeringId=" + offeringId
                        + ", enrollmentsCount=" + (enrollments == null ? 0 : enrollments.size())
                        + ", studentsCount=" + (students == null ? 0 : students.size()));
                model.addAttribute("students", students);
                // Provide an enrollmentMap for template lookup; populate map keyed by student
                // id
                java.util.Map<Long, Object> enrollmentMap = new java.util.HashMap<>();
                if (enrollments != null) {
                    for (Object o : enrollments) {
                        try {
                            com.course.entity.Enrollment e = (com.course.entity.Enrollment) o;
                            if (e != null && e.getStudent() != null && e.getStudent().getId() != null) {
                                enrollmentMap.put(e.getStudent().getId(), e);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
                model.addAttribute("enrollmentMap", enrollmentMap);
            } catch (Exception ex) {
                model.addAttribute("error", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred");
            }
        }
        return "views/lecturer/students";
    }

    /**
     * Render attendance page
     */
    @GetMapping("/attendance")
    public String attendance(
            @RequestParam(required = false) Long scheduleId,
            @RequestParam(required = false) Long lecturerId,
            Model model) {
        // TODO: After enabling security, get lecturerId from Authentication
        // If lecturer provided but no schedule selected, try to auto-select the first
        // schedule for one of the lecturer's offerings so the page shows useful data
        if (lecturerId != null && scheduleId == null) {
            try {
                var offerings = lecturerService.getOfferingsByLecturerId(lecturerId);
                if (offerings != null) {
                    for (var off : offerings) {
                        var classSchedules = classScheduleRepository.findByOfferingId(off.getId());
                        if (classSchedules != null && !classSchedules.isEmpty()) {
                            scheduleId = classSchedules.get(0).getId();
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (scheduleId != null && lecturerId != null) {
            model.addAttribute("scheduleId", scheduleId);
            model.addAttribute("lecturerId", lecturerId);
            model.addAttribute("attendanceRecords", lecturerService.getAttendanceRecords(scheduleId, lecturerId));
            // Also load enrolled students for this schedule's offering so lecturer can mark
            // attendance
            try {
                var schedOpt = classScheduleRepository.findById(scheduleId);
                if (schedOpt.isPresent() && schedOpt.get().getOffering() != null) {
                    Long offeringId = schedOpt.get().getOffering().getId();
                    var students = lecturerService.getEnrolledStudents(offeringId, lecturerId);
                    model.addAttribute("students", students);
                } else {
                    model.addAttribute("students", new java.util.ArrayList<>());
                }
            } catch (Exception ex) {
                model.addAttribute("students", new java.util.ArrayList<>());
            }
        }
        return "views/lecturer/attendance";
    }

    /**
     * Render schedule page
     */
    @GetMapping("/schedule")
    public String schedule(@RequestParam(required = false) Long lecturerId, Model model) {
        if (lecturerId != null) {
            model.addAttribute("lecturerId", lecturerId);
            // Fetch all offerings for this lecturer
            var offerings = lecturerService.getOfferingsByLecturerId(lecturerId);
            System.out.println("[DEBUG] Offerings for lecturerId=" + lecturerId + ":");
            for (var off : offerings) {
                System.out.println("  OfferingId=" + off.getId() + ", Course=" + off.getCourse().getCourseCode() + " - "
                        + off.getCourse().getTitle() + ", Term=" + off.getTerm().getTermName());
            }
            java.util.List<com.course.entity.ClassSchedule> schedules = new java.util.ArrayList<>();
            for (var offering : offerings) {
                var classSchedules = classScheduleRepository.findByOfferingId(offering.getId());
                System.out.println("    Schedules for OfferingId=" + offering.getId() + ": "
                        + (classSchedules != null ? classSchedules.size() : 0));
                for (var sched : classSchedules) {
                    System.out.println("      ScheduleId=" + sched.getId() + ", Day=" + sched.getDayOfWeek()
                            + ", Start=" + sched.getStartTime() + ", End=" + sched.getEndTime());
                }
                schedules.addAll(classSchedules);
            }
            System.out.println("[DEBUG] Total schedules found: " + schedules.size());
            model.addAttribute("schedules", schedules);
            model.addAttribute("offerings", offerings); // <-- Add offerings for course selection
        } else {
            model.addAttribute("offerings", new java.util.ArrayList<>());
        }
        // Always provide all terms for the modal
        model.addAttribute("terms", adminService.getAllTerms());
        return "views/lecturer/schedule";
    }

    /**
     * Render reports page
     */
    @GetMapping("/reports")
    public String reports(@RequestParam(required = false) Long lecturerId, Model model) {
        // TODO: After enabling security, get lecturerId from Authentication
        if (lecturerId != null) {
            model.addAttribute("lecturerId", lecturerId);
            // TODO: Fetch reports data from service when implemented
            // model.addAttribute("courseReports",
            // lecturerService.getCourseReports(lecturerId));
        }
        // Provide chart data specific to lecturer: attendance (30 days) and course
        // performance
        model.addAttribute("totalStudents", adminService.getTotalStudents());
        model.addAttribute("totalLecturers", adminService.getTotalLecturers());
        if (lecturerId != null) {
            var attendanceMap = lecturerService.getAttendanceCountsByDate(lecturerId, 30);
            var attendanceLabels = new java.util.ArrayList<String>(attendanceMap.keySet());
            var attendanceData = new java.util.ArrayList<Number>();
            for (String k : attendanceLabels) {
                attendanceData.add(attendanceMap.getOrDefault(k, 0L));
            }
            model.addAttribute("enrollmentLabels", attendanceLabels);
            model.addAttribute("enrollmentData", attendanceData);

            var perf = lecturerService.getCourseAverageGradeByLecturer(lecturerId);
            var courseLabels = new java.util.ArrayList<String>(perf.keySet());
            var courseData = new java.util.ArrayList<Number>();
            for (String k : courseLabels) {
                courseData.add(perf.getOrDefault(k, 0.0));
            }
            model.addAttribute("courseLabels", courseLabels);
            model.addAttribute("courseData", courseData);
        } else {
            model.addAttribute("enrollmentLabels", new java.util.ArrayList<String>());
            model.addAttribute("enrollmentData", new java.util.ArrayList<Number>());
            model.addAttribute("courseLabels", new java.util.ArrayList<String>());
            model.addAttribute("courseData", new java.util.ArrayList<Number>());
        }
        return "views/lecturer/reports";
    }
}
