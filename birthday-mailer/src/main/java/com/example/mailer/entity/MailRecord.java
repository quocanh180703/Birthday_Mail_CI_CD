package com.example.mailer.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
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

    // Tài khoản SMTP đã thực hiện gửi mail
    private String sentBy;

    private String lastError;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    public enum Status { PENDING, SENT, FAILED }
}
