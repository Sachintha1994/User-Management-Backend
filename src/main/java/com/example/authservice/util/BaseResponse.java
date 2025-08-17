package com.example.authservice.util;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BaseResponse <T> {

    private String status;
    private String message;
    private T data;
    private List<ErrorDetail> errors;

    public BaseResponse(String status, String message, T data, List<ErrorDetail> errors) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>("SUCCESS", message, data, null);
    }

    public static <T> BaseResponse<T> error(String message) {
        return new BaseResponse<>("ERROR", message, null, null);
    }

    public static <T> BaseResponse<T> failed(String message, List<ErrorDetail> errors) {
        return new BaseResponse<>("FAIL", message, null, errors);
    }
}
