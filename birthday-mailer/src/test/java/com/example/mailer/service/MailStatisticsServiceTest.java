package com.example.mailer.service;

import com.example.mailer.dto.MailRecordDto;
import com.example.mailer.dto.RecipientStatsDto;
import com.example.mailer.entity.MailRecord;
import com.example.mailer.repository.MailRecordRepository;
import com.example.mailer.service.impl.MailStatisticsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MailStatisticsServiceImpl Tests")
class MailStatisticsServiceTest {

    @Mock MailRecordRepository repo;
    @InjectMocks MailStatisticsServiceImpl service;

    // ─── getStatsByRecipient ──────────────────────────────────────────────────

    @Test
    @DisplayName("getStatsByRecipient — ánh xạ đúng các cột thống kê từ native query")
    void getStatsByRecipient_mapsRowsCorrectly() {
        Object[] row = {
                "alice@example.com",
                "noreply@company.com",
                3L,    // totalSent
                2L,    // successCount
                1L,    // failedCount
                7L,    // totalAttempts
                3.50,  // avgAttemptsOnSuccess
                "2026-06-18 08:00:00"
        };

        when(repo.findSendingStatsByRecipient()).thenReturn(java.util.Collections.singletonList(row));

        List<RecipientStatsDto> stats = service.getStatsByRecipient();

        assertThat(stats).hasSize(1);
        RecipientStatsDto s = stats.get(0);
        assertThat(s.recipient()).isEqualTo("alice@example.com");
        assertThat(s.sentBy()).isEqualTo("noreply@company.com");
        assertThat(s.totalSent()).isEqualTo(3L);
        assertThat(s.successCount()).isEqualTo(2L);
        assertThat(s.failedCount()).isEqualTo(1L);
        assertThat(s.totalAttempts()).isEqualTo(7L);
        assertThat(s.avgAttemptsOnSuccess()).isEqualTo(3.50);
        assertThat(s.lastSentAt()).isEqualTo("2026-06-18 08:00:00");
    }

    @Test
    @DisplayName("getStatsByRecipient — avgAttemptsOnSuccess = 0.0 khi null từ DB")
    void getStatsByRecipient_nullAvgAttempts_returnsZero() {
        Object[] row = {
                "bob@example.com", "noreply@company.com",
                2L, 0L, 2L, 6L,
                null,  // avgAttemptsOnSuccess null = chưa SENT lần nào
                "2026-06-01 09:00:00"
        };

        when(repo.findSendingStatsByRecipient()).thenReturn(java.util.Collections.singletonList(row));

        List<RecipientStatsDto> stats = service.getStatsByRecipient();
        assertThat(stats.get(0).avgAttemptsOnSuccess()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("getStatsByRecipient — trả về list rỗng khi bảng trống")
    void getStatsByRecipient_empty() {
        when(repo.findSendingStatsByRecipient()).thenReturn(List.of());
        assertThat(service.getStatsByRecipient()).isEmpty();
    }

    // ─── getHistoryByRecipient ────────────────────────────────────────────────

    @Test
    @DisplayName("getHistoryByRecipient — trả về lịch sử đúng recipient")
    void getHistoryByRecipient_returnsMatchingRecords() {
        MailRecord r1 = new MailRecord();
        r1.setId(10L);
        r1.setRecipient("carol@example.com");
        r1.setSubject("Happy Birthday Carol! 🎉");
        r1.setCreatedAt(LocalDateTime.of(2026, 6, 18, 8, 0));
        r1.setStatus(MailRecord.Status.SENT);

        MailRecord r2 = new MailRecord();
        r2.setId(11L);
        r2.setRecipient("carol@example.com");
        r2.setSubject("Happy Birthday Carol! 🎉");
        r2.setCreatedAt(LocalDateTime.of(2025, 6, 18, 8, 0));
        r2.setStatus(MailRecord.Status.FAILED);

        when(repo.findAllByRecipient("carol@example.com")).thenReturn(List.of(r1, r2));

        List<MailRecordDto> history = service.getHistoryByRecipient("carol@example.com");

        assertThat(history).hasSize(2);
        assertThat(history.get(0).status()).isEqualTo("SENT");
        assertThat(history.get(1).status()).isEqualTo("FAILED");
    }

    @Test
    @DisplayName("getHistoryByRecipient — trả về list rỗng khi không có bản ghi")
    void getHistoryByRecipient_unknownRecipient_returnsEmpty() {
        when(repo.findAllByRecipient("unknown@x.com")).thenReturn(List.of());
        assertThat(service.getHistoryByRecipient("unknown@x.com")).isEmpty();
    }
}
