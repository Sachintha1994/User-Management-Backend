package com.example.authservice.web.dto;

import com.example.authservice.enums.UserRoles;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String address;
    private UserRoles role;
    private boolean emailVerified;
    private boolean accountLocked;
    private LocalDateTime lastLogin;
}
