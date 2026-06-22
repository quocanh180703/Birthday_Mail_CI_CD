package com.example.mailer.exception;

import com.example.mailer.controller.MailRecordController;
import com.example.mailer.service.IMailRecordService;
import com.example.mailer.service.IMailStatisticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MailRecordController.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @Autowired MockMvc mvc;

    @MockBean IMailRecordService mailRecordService;
    @MockBean IMailStatisticsService statisticsService;

    // ─── InvalidMailStatusException → 400 ────────────────────────────────────

    @Test
    @DisplayName("Status không hợp lệ — trả 400 với message rõ ràng")
    void invalidStatus_returns400() throws Exception {
        mvc.perform(get("/api/mail-records/recent-by-status")
                        .param("status", "UNKNOWN")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("UNKNOWN")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ─── MissingServletRequestParameterException → 400 ───────────────────────

    @Test
    @DisplayName("Thiếu param bắt buộc — trả 400")
    void missingParam_returns400() throws Exception {
        mvc.perform(get("/api/mail-records/recent-by-status")
                        // không truyền param status
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("status")));
    }

    // ─── Exception tổng quát → 500 ───────────────────────────────────────────

    @Test
    @DisplayName("Lỗi không xác định từ service — trả 500")
    void unexpectedException_returns500() throws Exception {
        when(statisticsService.getStatsByRecipient()).thenThrow(new RuntimeException("DB down"));

        mvc.perform(get("/api/mail-records/stats")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    // ─── Request hợp lệ vẫn hoạt động bình thường ────────────────────────────

    @Test
    @DisplayName("Status hợp lệ SENT — trả 200")
    void validStatus_returns200() throws Exception {
        when(mailRecordService.getRecentByStatusDto("SENT", 10)).thenReturn(List.of());

        mvc.perform(get("/api/mail-records/recent-by-status")
                        .param("status", "SENT")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
