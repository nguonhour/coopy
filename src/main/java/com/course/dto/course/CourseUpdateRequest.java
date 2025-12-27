package com.course.dto.course;

import java.util.List;

import lombok.Data;

@Data
public class CourseUpdateRequest {
    private String courseCode;
    private String title;
    private String description;
    private Integer credits;
    private Boolean active;

    // List of lecturer IDs for direct assignment
    private List<Long> lecturerIds;
}
