package com.example.mailer.controller;

import com.example.mailer.entity.MailRecord;
import com.example.mailer.repository.MailRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.mail.username=noreply@company.com"
})
@DisplayName("MailRecordController Integration Tests")
class MailRecordControllerIntegrationTest {

    @MockBean private JavaMailSender javaMailSender;
    @MockBean private ConnectionFactory connectionFactory;

    @Autowired private MockMvc mvc;
    @Autowired private MailRecordRepository repo;

    @BeforeEach
    void setup() {
        repo.deleteAll();

        // alice — SENT 2 lần (lần 1 thử 1 lần, lần 2 thử 2 lần)
        repo.save(record("alice@example.com", "noreply@company.com", MailRecord.Status.SENT, 1, null,
                LocalDateTime.now().minusDays(2)));
        repo.save(record("alice@example.com", "noreply@company.com", MailRecord.Status.SENT, 2, null,
                LocalDateTime.now().minusDays(1)));

        // bob — FAILED
        repo.save(record("bob@example.com", "noreply@company.com", MailRecord.Status.FAILED, 3, "SMTP timeout",
                LocalDateTime.now()));

        // carol — SENT 1 lần
        repo.save(record("carol@example.com", "noreply@company.com", MailRecord.Status.SENT, 1, null,
                LocalDateTime.now().minusHours(5)));
    }

    // ─── GET /recent-by-status ────────────────────────────────────────────────

    @Test
    @DisplayName("GET /recent-by-status?status=SENT&limit=2 — trả đúng số lượng")
    void recentByStatus_returnsLimitedSentRecords() throws Exception {
        mvc.perform(get("/api/mail-records/recent-by-status")
                        .param("status", "SENT")
                        .param("limit", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /recent-by-status?status=FAILED — trả đúng bản ghi FAILED")
    void recentByStatus_failedStatus() throws Exception {
        mvc.perform(get("/api/mail-records/recent-by-status")
                        .param("status", "FAILED")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].recipient").value("bob@example.com"))
                .andExpect(jsonPath("$[0].status").value("FAILED"));
    }

    // ─── GET /stats ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /stats — trả thống kê đúng số recipient")
    void stats_returnsOneRowPerRecipient() throws Exception {
        mvc.perform(get("/api/mail-records/stats")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))); // alice, bob, carol
    }

    @Test
    @DisplayName("GET /stats — alice: successCount=2, failedCount=0, totalAttempts=3")
    void stats_aliceHasCorrectCounts() throws Exception {
        mvc.perform(get("/api/mail-records/stats")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.recipient=='alice@example.com')].successCount",
                        contains(2)))
                .andExpect(jsonPath("$[?(@.recipient=='alice@example.com')].failedCount",
                        contains(0)))
                .andExpect(jsonPath("$[?(@.recipient=='alice@example.com')].totalAttempts",
                        contains(3)));
    }

    @Test
    @DisplayName("GET /stats — bob: successCount=0, failedCount=1")
    void stats_bobAllFailed() throws Exception {
        mvc.perform(get("/api/mail-records/stats")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.recipient=='bob@example.com')].successCount",
                        contains(0)))
                .andExpect(jsonPath("$[?(@.recipient=='bob@example.com')].failedCount",
                        contains(1)));
    }

    // ─── GET /history ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /history?recipient=alice@example.com — trả đúng 2 bản ghi")
    void history_returnsAllRecordsForRecipient() throws Exception {
        mvc.perform(get("/api/mail-records/history")
                        .param("recipient", "alice@example.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].recipient", everyItem(is("alice@example.com"))));
    }

    @Test
    @DisplayName("GET /history?recipient=unknown@x.com — trả list rỗng")
    void history_unknownRecipient_returnsEmpty() throws Exception {
        mvc.perform(get("/api/mail-records/history")
                        .param("recipient", "unknown@x.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private MailRecord record(String recipient, String sentBy, MailRecord.Status status,
                              int attempts, String lastError, LocalDateTime createdAt) {
        MailRecord r = new MailRecord();
        r.setRecipient(recipient);
        r.setSubject("Happy Birthday!");
        r.setSentBy(sentBy);
        r.setStatus(status);
        r.setAttempts(attempts);
        r.setLastError(lastError);
        r.setCreatedAt(createdAt);
        if (status == MailRecord.Status.SENT) r.setSentAt(createdAt.plusSeconds(5));
        return r;
    }
}
