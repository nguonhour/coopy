package com.course.dto.course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicTermResponseDTO {
    private Long id;
    private String termCode;
    private String termName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
}
