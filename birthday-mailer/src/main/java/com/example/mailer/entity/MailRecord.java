package com.example.mailer.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mail_record")
@Getter
@Setter
@NoArgsConstructor
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

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    public enum Status { PENDING, SENT, FAILED }
}
