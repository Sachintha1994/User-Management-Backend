package com.example.authservice.domain.user;

import com.example.authservice.domain.audit.Auditable;
import com.example.authservice.enums.UserRoles;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends Auditable implements UserDetails {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Column
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Column
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Column
    private LocalDate dateOfBirth;

    @Column
    private String address;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column
    private boolean accountLocked = false;

    @Column
    private LocalDateTime lastLogin;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRoles role;

    @Column
    private String profilePictureUrl;

    @Column
    private String resetPasswordToken;

    @Column
    private LocalDateTime resetPasswordTokenExpiry;

    @Column(nullable = false)
    private boolean twoFactorEnabled = false;

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return emailVerified;
    }
}