package com.course.dto.user;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String idCard;
    private Long roleId;
    private Boolean active;
    private String password; // Optional - only if changing password
}
