package com.example.mailer.controller;

import com.example.mailer.entity.MailRecord;
import com.example.mailer.exception.InvalidMailStatusException;
import com.example.mailer.service.IMailRecordService;
import com.example.mailer.service.IMailStatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mail-records")
public class MailRecordController {

    private static final Set<String> VALID_STATUSES = Arrays.stream(MailRecord.Status.values())
            .map(Enum::name)
            .collect(Collectors.toSet());

    private final IMailRecordService mailRecordService;
    private final IMailStatisticsService statisticsService;

    public MailRecordController(IMailRecordService mailRecordService,
                                IMailStatisticsService statisticsService) {
        this.mailRecordService = mailRecordService;
        this.statisticsService = statisticsService;
    }

    /**
     * Lấy danh sách mail gần đây theo status (PENDING / SENT / FAILED)
     * GET /api/mail-records/recent-by-status?status=SENT&limit=10
     */
    @GetMapping("/recent-by-status")
    public ResponseEntity<?> recentByStatus(
            @RequestParam String status,
            @RequestParam(defaultValue = "10") int limit) {
        String upperStatus = status.toUpperCase();
        if (!VALID_STATUSES.contains(upperStatus)) {
            throw new InvalidMailStatusException(status);
        }
        return ResponseEntity.ok(mailRecordService.getRecentByStatusDto(upperStatus, limit));
    }

    /**
     * Thống kê tổng hợp theo từng recipient
     * GET /api/mail-records/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        return ResponseEntity.ok(statisticsService.getStatsByRecipient());
    }

    /**
     * Lịch sử từng lần gửi của một recipient cụ thể
     * GET /api/mail-records/history?recipient=alice@example.com
     */
    @GetMapping("/history")
    public ResponseEntity<?> history(@RequestParam String recipient) {
        return ResponseEntity.ok(statisticsService.getHistoryByRecipient(recipient));
    }
}
