package com.example.mailer.dto;

import java.time.LocalDateTime;

/**
 * Response body chuẩn khi có lỗi xảy ra.
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, LocalDateTime.now());
    }
}
