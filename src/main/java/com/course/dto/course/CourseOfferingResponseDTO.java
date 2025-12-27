package com.course.dto.course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseOfferingResponseDTO {
    private Long id;
    private Long courseId;
    private String courseCode;
    private String courseTitle;
    private Long termId;
    private String termCode;
    private String termName;
    private Integer capacity;
    private Long enrolledCount;
    private Boolean isActive;
}
