package com.example.authservice.common.exception;

import lombok.*;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ApiError {
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
