package com.example.mailer.service.impl;

import com.example.mailer.dto.MailRecordDto;
import com.example.mailer.dto.RecipientStatsDto;
import com.example.mailer.repository.MailRecordRepository;
import com.example.mailer.service.IMailStatisticsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MailStatisticsServiceImpl implements IMailStatisticsService {

    private final MailRecordRepository repo;

    public MailStatisticsServiceImpl(MailRecordRepository repo) {
        this.repo = repo;
    }

    // ─── Thống kê tổng hợp theo recipient ────────────────────────────────────

    @Override
    public List<RecipientStatsDto> getStatsByRecipient() {
        return repo.findSendingStatsByRecipient().stream()
                .map(row -> new RecipientStatsDto(
                        (String) row[0],                             // recipient
                        (String) row[1],                             // sentBy
                        toLong(row[2]),                              // totalSent
                        toLong(row[3]),                              // successCount
                        toLong(row[4]),                              // failedCount
                        toLong(row[5]),                              // totalAttempts
                        toDouble(row[6]),                            // avgAttemptsOnSuccess
                        row[7] != null ? row[7].toString() : null    // lastSentAt (string từ native)
                ))
                .collect(Collectors.toList());
    }

    // ─── Lịch sử gửi của một recipient ───────────────────────────────────────

    @Override
    public List<MailRecordDto> getHistoryByRecipient(String recipient) {
        return repo.findAllByRecipient(recipient).stream()
                .map(r -> new MailRecordDto(
                        r.getId(),
                        r.getRecipient(),
                        r.getSubject(),
                        r.getCreatedAt(),
                        r.getStatus().name()))
                .collect(Collectors.toList());
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        BigDecimal bd = new BigDecimal(val.toString()).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
