package com.example.authservice.web.dto;

import lombok.*;

@Data
@Builder
@Getter
@Setter
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;


}
