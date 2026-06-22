package com.example.mailer.dto;

import java.time.LocalDateTime;

// DTO đại diện cho một bản ghi mail — dùng trong query theo status và lịch sử gửi
public record MailRecordDto(
        Long id,
        String recipient,
        String subject,
        LocalDateTime createdAt,
        String status
) {}
