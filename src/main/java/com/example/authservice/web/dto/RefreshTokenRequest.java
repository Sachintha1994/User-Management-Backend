package com.example.authservice.web.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
