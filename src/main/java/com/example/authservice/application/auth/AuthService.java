package com.example.authservice.application.auth;

import com.example.authservice.web.dto.*;

import java.util.List;

public interface AuthService {
    UserResponse register(RegisterRequest req);

    JwtResponse login(LoginRequest req);

    JwtResponse refresh(RefreshTokenRequest req);

    void logout(RefreshTokenRequest req);

    List<UserResponse> getAllUsers();

    UserResponse getUserByFilter(String email, String phoneNumber);
}
