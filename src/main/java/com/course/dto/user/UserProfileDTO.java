package com.course.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private Long userId;
    private String phone;
    private LocalDate dateOfBirth;
    private String bio;
    private String avatarUrl;
}
