package com.example.mailer.dto;

/**
 * DTO thống kê tổng hợp theo recipient + tài khoản gửi.
 *
 * @param avgAttemptsOnSuccess trung bình số lần thử mới gửi thành công
 *                             (0.0 = chưa có lần nào thành công)
 */
public record RecipientStatsDto(
        String recipient,
        String sentBy,
        long   totalSent,
        long   successCount,
        long   failedCount,
        long   totalAttempts,
        double avgAttemptsOnSuccess,
        String lastSentAt
) {}
