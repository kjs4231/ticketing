package com.ticket.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(400, "C001", "Invalid Input Value"),
    INTERNAL_SERVER_ERROR(500, "C002", "Internal Server Error"),

    // Event
    EVENT_NOT_FOUND(404, "E001", "Event Not Found"),

    // Seat
    SEAT_NOT_AVAILABLE(400, "S001", "Seat Not Available"),

    // Payment
    PAYMENT_FAILED(400, "P001", "Payment Failed");

    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}