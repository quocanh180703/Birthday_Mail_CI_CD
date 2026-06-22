package com.example.mailer.service;

import com.example.mailer.entity.MailRecord;
import com.example.mailer.exception.MailPersistenceException;
import com.example.mailer.exception.MailSendException;
import com.example.mailer.model.Employee;
import com.example.mailer.repository.MailRecordRepository;
import com.example.mailer.service.impl.MailServiceImpl;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MailServiceImpl Tests")
class MailServiceTest {

    @Mock private JavaMailSender mailSender;
    @Mock private MailRecordRepository repo;
    @Mock private TemplateEngine templateEngine;
    @Mock private MimeMessage mimeMessage;

    private MailServiceImpl mailService;

    private static final String SENDER_ACCOUNT = "noreply@company.com";

    @BeforeEach
    void setUp() {
        mailService = new MailServiceImpl(mailSender, repo, templateEngine);
        ReflectionTestUtils.setField(mailService, "senderAccount", SENDER_ACCOUNT);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Employee employee(String name, String email) {
        return new Employee(name, email, LocalDate.of(1990, 6, 18), null);
    }

    private MailRecord savedRecord(Long id) {
        MailRecord r = new MailRecord();
        r.setId(id);
        r.setRecipient("alice@example.com");
        r.setSubject("Happy Birthday Alice! 🎉");
        r.setStatus(MailRecord.Status.PENDING);
        r.setAttempts(0);
        r.setSentBy(SENDER_ACCOUNT);
        return r;
    }

    // ─── Test: Gửi thành công ngay lần đầu ───────────────────────────────────

    @Test
    @DisplayName("Gửi thành công lần 1 — record được lưu SENT với sentBy đúng")
    void sendBirthdayEmail_successOnFirstAttempt_savesRecordAsSent() {
        Employee emp = employee("Alice", "alice@example.com");
        MailRecord pending = savedRecord(1L);

        when(templateEngine.process(eq("birthday-email"), any(Context.class))).thenReturn("<html>Hi Alice</html>");
        when(repo.save(any())).thenReturn(pending);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        mailService.sendBirthdayEmail(emp);

        ArgumentCaptor<MailRecord> captor = ArgumentCaptor.forClass(MailRecord.class);
        verify(repo, atLeast(2)).save(captor.capture());

        MailRecord lastSaved = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertThat(lastSaved.getStatus()).isEqualTo(MailRecord.Status.SENT);
        assertThat(lastSaved.getAttempts()).isEqualTo(1);
        assertThat(lastSaved.getSentAt()).isNotNull();
        assertThat(lastSaved.getLastError()).isNull();
    }

    // ─── Test: sentBy được set đúng tài khoản SMTP ───────────────────────────

    @Test
    @DisplayName("sentBy phải là tài khoản SMTP đang cấu hình")
    void sendBirthdayEmail_sentBy_isSetToSenderAccount() {
        Employee emp = employee("Bob", "bob@example.com");
        when(templateEngine.process(eq("birthday-email"), any(Context.class))).thenReturn("<html>Hi Bob</html>");
        when(repo.save(any())).thenAnswer(inv -> {
            MailRecord r = inv.getArgument(0);
            r.setId(99L);
            return r;
        });
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        mailService.sendBirthdayEmail(emp);

        ArgumentCaptor<MailRecord> captor = ArgumentCaptor.forClass(MailRecord.class);
        verify(repo, atLeast(1)).save(captor.capture());

        MailRecord firstSave = captor.getAllValues().get(0);
        assertThat(firstSave.getSentBy()).isEqualTo(SENDER_ACCOUNT);
    }

    // ─── Test: Gửi thất bại hết 3 lần → FAILED + throw MailSendException ─────

    @Test
    @DisplayName("Gửi thất bại cả 3 lần — record FAILED và throw MailSendException")
    void sendBirthdayEmail_allAttemptsFail_throwsMailSendException() {
        Employee emp = employee("Carol", "carol@example.com");
        MailRecord pending = savedRecord(2L);

        when(templateEngine.process(eq("birthday-email"), any(Context.class))).thenReturn("<html>Hi Carol</html>");
        when(repo.save(any())).thenReturn(pending);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("SMTP timeout")).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> mailService.sendBirthdayEmail(emp))
                .isInstanceOf(MailSendException.class)
                .hasMessageContaining("carol@example.com")
                .satisfies(ex -> {
                    MailSendException mse = (MailSendException) ex;
                    assertThat(mse.getAttempts()).isEqualTo(3);
                    assertThat(mse.getRecipient()).isEqualTo("carol@example.com");
                });

        // Bản ghi cuối phải là FAILED
        ArgumentCaptor<MailRecord> captor = ArgumentCaptor.forClass(MailRecord.class);
        verify(repo, atLeast(2)).save(captor.capture());
        MailRecord lastSaved = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertThat(lastSaved.getStatus()).isEqualTo(MailRecord.Status.FAILED);
        assertThat(lastSaved.getLastError()).contains("SMTP timeout");
    }

    // ─── Test: Lỗi DB khi lưu PENDING → throw MailPersistenceException ────────

    @Test
    @DisplayName("Lỗi DB khi lưu PENDING — throw MailPersistenceException, không gọi mailSender")
    void sendBirthdayEmail_dbFailOnPending_throwsMailPersistenceException() {
        Employee emp = employee("Dave", "dave@example.com");
        when(templateEngine.process(eq("birthday-email"), any(Context.class))).thenReturn("<html>Hi Dave</html>");
        when(repo.save(any())).thenThrow(new RuntimeException("DB connection refused"));

        assertThatThrownBy(() -> mailService.sendBirthdayEmail(emp))
                .isInstanceOf(MailPersistenceException.class)
                .hasMessageContaining("dave@example.com");

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    // ─── Test: attempts tăng dần qua mỗi lần retry ───────────────────────────

    @Test
    @DisplayName("attempts được cập nhật đúng sau mỗi lần retry thất bại")
    void sendBirthdayEmail_attemptsIncrementedOnEachFailure() {
        Employee emp = employee("Eve", "eve@example.com");
        
        // Tạo một List để lưu lịch sử biến thiên của attempts ngay lúc save
        List<Integer> savedAttemptsHistory = new ArrayList<>();
        
        when(templateEngine.process(eq("birthday-email"), any(Context.class))).thenReturn("<html>Hi Eve</html>");
        
        when(repo.save(any())).thenAnswer(inv -> {
            MailRecord r = inv.getArgument(0);
            
            // Chụp lại giá trị attempts HIỆN TẠI ở thời điểm hàm save() được gọi
            savedAttemptsHistory.add(r.getAttempts()); 
            
            if (r.getId() == null) r.setId(3L);
            return r;
        });
        
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("fail")).when(mailSender).send(any(MimeMessage.class));

        // Act & Assert Exception
        assertThatThrownBy(() -> mailService.sendBirthdayEmail(emp))
                .isInstanceOf(MailSendException.class);

        // Đổi từ 4 thành 5 lần
        verify(repo, times(5)).save(any(MailRecord.class)); 

        // Lần save thứ 5 (chuyển sang FAILED) thì attempts không tăng thêm, vẫn là 3.
        // Nên mảng lịch sử của bạn sẽ có dạng: 0, 1, 2, 3, 3
        assertThat(savedAttemptsHistory).containsExactly(0, 1, 2, 3, 3);
    }
}
