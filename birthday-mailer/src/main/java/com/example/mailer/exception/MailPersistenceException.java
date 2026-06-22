package com.example.mailer.exception;

// Ném ra khi không thể lưu bản ghi mail vào database

public class MailPersistenceException extends RuntimeException {

    private final String recipient;

    public MailPersistenceException(String recipient, Throwable cause) {
        super(String.format("Không thể lưu bản ghi mail cho '%s' vào database", recipient), cause);
        this.recipient = recipient;
    }

    public String getRecipient() {
        return recipient;
    }
}
