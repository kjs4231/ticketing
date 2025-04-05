package com.ticket.common.response;

import com.ticket.common.exception.ErrorCode;
import lombok.Getter;

import java.util.Objects;

@Getter
public class ApiResponse<T> {
    private final String status;
    private final String code;
    private final String message;
    private final T data;

    private ApiResponse(String status, String code, String message, T data) {
        this.status = Objects.requireNonNull(status);
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", null, null, data);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>("ERROR", errorCode.getCode(), errorCode.getMessage(), null);
    }
}