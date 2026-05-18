package com.example.mailer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mail_record")
public class MailRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String recipient;
    private String subject;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String body;

    private int attempts;

    private String lastError;

    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status { PENDING, SENT, FAILED }

    public MailRecord() {
        this.createdAt = LocalDateTime.now();
        this.status = Status.PENDING;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
