package com.example.mailer.exception;

/**
 * Ném ra khi địa chỉ email không hợp lệ về định dạng hoặc domain không tồn tại.
 */
public class InvalidEmailException extends RuntimeException {

    private final String email;

    public InvalidEmailException(String email, String reason) {
        super(String.format("Địa chỉ email không hợp lệ '%s': %s", email, reason));
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
