package com.course.controller;

import com.course.entity.CourseOffering;
import com.course.entity.Enrollment;
import com.course.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentApiController {

    private final StudentService studentService;

    @GetMapping("/{studentId}/available-courses")
    public ResponseEntity<List<CourseOffering>> getAvailableCourses(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false) String keyword) {
        var list = studentService.getAvailableOfferings(studentId, termId, keyword);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{studentId}/enroll/{offeringId}")
    public ResponseEntity<?> enrollInOffering(@PathVariable Long studentId, @PathVariable Long offeringId) {
        try {
            Enrollment e = studentService.enrollInOffering(studentId, offeringId);
            return ResponseEntity.ok(e);
        } catch (IllegalArgumentException ia) {
            return ResponseEntity.badRequest().body(ia.getMessage());
        } catch (IllegalStateException ise) {
            return ResponseEntity.status(409).body(ise.getMessage());
        }
    }
}
