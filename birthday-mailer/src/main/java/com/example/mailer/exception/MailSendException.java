package com.example.mailer.exception;

// Ném ra khi đã thử đủ số lần retry nhưng không gửi mail thành công.

public class MailSendException extends RuntimeException {

    private final String recipient;
    private final int attempts;

    public MailSendException(String recipient, int attempts, Throwable cause) {
        super(String.format("Không thể gửi mail tới '%s' sau %d lần thử", recipient, attempts), cause);
        this.recipient = recipient;
        this.attempts = attempts;
    }

    public String getRecipient() {
        return recipient;
    }

    public int getAttempts() {
        return attempts;
    }
}
