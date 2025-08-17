package com.example.authservice.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ErrorDetail {

    private String field;
    private String error;

    public ErrorDetail(String field, String defaultMessage) {
    }
}
