package com.example.authservice.application.auth.impl;

import com.example.authservice.application.auth.AuthService;
import com.example.authservice.domain.token.*;
import com.example.authservice.domain.user.*;
import com.example.authservice.enums.UserRoles;
import com.example.authservice.security.JwtService;
import com.example.authservice.web.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refreshDays:7}")
    private long refreshDays;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager, JwtService jwtService,
                           RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAddress(request.getAddress());
        user.setRole(UserRoles.USER);
        user.setEmailVerified(false);
        user.setAccountLocked(false);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public JwtResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        } catch (DisabledException e) {
            throw new IllegalStateException("Email not verified");
        } catch (LockedException e) {
            throw new IllegalStateException("Account is locked");
        } catch (Exception e) {
            throw new IllegalStateException("Authentication failed: " + e.getMessage());
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        var claims = new HashMap<String, Object>();
        claims.put("uid", user.getId().toString());
        claims.put("email_verified", user.isEmailVerified());
        claims.put("role", user.getRole());

        String accessToken = jwtService.buildAccessToken(user.getEmail(), claims);
        String refreshToken = UUID.randomUUID().toString();
        RefreshToken rt = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiresAt(Instant.now().plus(refreshDays, ChronoUnit.DAYS))
                .revoked(false)
                .createdAt(Instant.now())
                .build();
        refreshTokenRepository.save(rt);

        user.setLastLogin(Instant.now().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        userRepository.save(user);

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    @Override
    @Transactional
    public JwtResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token expired or revoked");
        }

        User user = refreshToken.getUser();
        if (!user.isEmailVerified()) {
            throw new IllegalStateException("Email not verified");
        }

        if (user.isAccountLocked()) {
            throw new IllegalStateException("Account is locked");
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        var claims = new HashMap<String, Object>();
        claims.put("uid", user.getId().toString());
        claims.put("email_verified", user.isEmailVerified());
        claims.put("role", user.getRole());

        String newAccessToken = jwtService.buildAccessToken(user.getEmail(), claims);
        String newRefreshToken = UUID.randomUUID().toString();
        RefreshToken newRt = RefreshToken.builder()
                .token(newRefreshToken)
                .user(user)
                .expiresAt(Instant.now().plus(refreshDays, ChronoUnit.DAYS))
                .revoked(false)
                .createdAt(Instant.now())
                .build();
        refreshTokenRepository.save(newRt);

        return JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .build();
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByToken(request.getRefreshToken()).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByFilter(String email, String phoneNumber) {
        if ((email == null || email.isBlank()) && (phoneNumber == null || phoneNumber.isBlank())) {
            throw new IllegalArgumentException("Either email or phoneNumber must be provided");
        }

        Optional<User> userOptional;

        if (email != null && !email.isBlank() && phoneNumber != null && !phoneNumber.isBlank()) {
            // Fetch by both email AND phone number
            userOptional = userRepository.findByEmailAndPhoneNumber(email, phoneNumber);
        } else if (email != null && !email.isBlank()) {
            // Fetch by email only
            userOptional = userRepository.findByEmail(email);
        } else {
            // Fetch by phone number only
            userOptional = userRepository.findByPhoneNumber(phoneNumber);
        }

        User user = userOptional.orElseThrow(() ->
                new IllegalArgumentException("User not found with provided parameters"));

        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth())
                .address(user.getAddress())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .accountLocked(user.isAccountLocked())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
