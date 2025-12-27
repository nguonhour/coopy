package com.course.dto.course;

import java.util.List;

import lombok.Data;

@Data
public class CourseCreateRequest {
    private String courseCode;
    private String title;
    private String description;
    private int credits;
    private boolean active = true;

    // List of lecturer IDs for direct assignment
    private List<Long> lecturerIds;
}
