package com.example.mailer.exception;

// Ném ra khi client truyền giá trị status không hợp lệ (khác PENDING / SENT / FAILED).

public class InvalidMailStatusException extends RuntimeException {

    private final String invalidStatus;

    public InvalidMailStatusException(String invalidStatus) {
        super(String.format("Trạng thái mail không hợp lệ: '%s'. Giá trị hợp lệ: PENDING, SENT, FAILED",
                invalidStatus));
        this.invalidStatus = invalidStatus;
    }

    public String getInvalidStatus() {
        return invalidStatus;
    }
}
