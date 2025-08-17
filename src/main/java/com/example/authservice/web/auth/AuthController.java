package com.example.authservice.web.auth;

import com.example.authservice.application.auth.AuthService;
import com.example.authservice.util.BaseResponse;
import com.example.authservice.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("User registered successfully. Please verify your email.");
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<JwtResponse>> login(@RequestBody @Valid LoginRequest request) {
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(BaseResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<JwtResponse>> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        JwtResponse response = authService.refresh(request);
        return ResponseEntity.ok(BaseResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<String>> logout(@RequestBody @Valid RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(BaseResponse.success("Logged out successfully", "Logged out successfully"));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = authService.getAllUsers();
        return ResponseEntity.ok(BaseResponse.success("Users fetched successfully", users));
    }

    @GetMapping("/users/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<UserResponse>> getUserByFilter(
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber) {
        UserResponse user = authService.getUserByFilter(email, phoneNumber);
        return ResponseEntity.ok(BaseResponse.success("User fetched successfully", user));
    }

}
