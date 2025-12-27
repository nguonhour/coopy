package com.course.dto.course;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseOfferingRequestDTO {
    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Term ID is required")
    private Long termId;

    @NotNull(message = "Capacity is required")
    private Integer capacity;

    private Boolean active;
}
