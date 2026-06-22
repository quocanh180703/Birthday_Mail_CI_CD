package com.example.mailer.service.impl;

import com.example.mailer.entity.MailRecord;
import com.example.mailer.exception.MailPersistenceException;
import com.example.mailer.exception.MailSendException;
import com.example.mailer.model.Employee;
import com.example.mailer.repository.MailRecordRepository;
import com.example.mailer.service.IMailService;
import javax.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.time.LocalDateTime;

@Slf4j
@Service
public class MailServiceImpl implements IMailService {

    private final JavaMailSender mailSender;
    private final MailRecordRepository repo;
    private final TemplateEngine templateEngine;

    // Tài khoản SMTP đang được dùng để gửi — để ghi vào mail_record.sent_by
    @Value("${spring.mail.username}")
    private String senderAccount;

    public MailServiceImpl(JavaMailSender mailSender,
                           MailRecordRepository repo,
                           TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.repo = repo;
        this.templateEngine = templateEngine;
    }

    @Override
    @Transactional
    public void sendBirthdayEmail(Employee e) {
        log.info("[BẮT ĐẦU] Quy trình gửi mail cho: {}", e.getEmail());

        // 1. Render template HTML
        Context context = new Context();
        context.setVariable("name", e.getName());
        boolean hasImage = e.getImagePath() != null
                && !e.getImagePath().isBlank()
                && new File(e.getImagePath()).exists();
        context.setVariable("hasImage", hasImage);

        String htmlContent = templateEngine.process("birthday-email", context);

        // 2. Lưu bản ghi PENDING vào DB
        MailRecord record;
        try {
            MailRecord pending = new MailRecord();
            pending.setRecipient(e.getEmail());
            pending.setSubject("Happy Birthday " + e.getName() + "! 🎉");
            pending.setBody(htmlContent);
            pending.setSentBy(senderAccount);
            pending.setAttempts(0);
            pending.setStatus(MailRecord.Status.PENDING);
            record = repo.save(pending);
            log.debug("Đã lưu bản ghi PENDING. ID: {}", record.getId());
        } catch (Exception dbEx) {
            throw new MailPersistenceException(e.getEmail(), dbEx);
        }

        // 3. Retry loop
        int maxAttempts = 3;
        long backoffMs = 2000L;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.debug("Đang thử gửi mail lần {} cho: {}", attempt, e.getEmail());

                MimeMessage msg = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
                helper.setTo(e.getEmail());
                helper.setSubject(record.getSubject());
                helper.setText(htmlContent, true);

                if (hasImage) {
                    FileSystemResource res = new FileSystemResource(new File(e.getImagePath()));
                    helper.addInline("birthday_img", res);
                }

                mailSender.send(msg);

                // Gửi thành công
                record.setAttempts(attempt);
                record.setStatus(MailRecord.Status.SENT);
                record.setSentAt(LocalDateTime.now());
                record.setLastError(null);
                repo.save(record);

                log.info("[THÀNH CÔNG] Mail tới {} — tài khoản: {} — lần thử: {}",
                        e.getEmail(), senderAccount, attempt);
                return;

            } catch (Exception ex) {
                lastException = ex;
                log.warn("Thất bại lần {} cho {}: {}", attempt, e.getEmail(), ex.getMessage());
                record.setAttempts(attempt);
                record.setLastError(ex.getMessage());
                repo.save(record);

                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("Retry bị gián đoạn cho: {}", e.getEmail());
                    }
                    backoffMs *= 2; // exponential backoff
                }
            }
        }

        // Hết retry — đánh dấu FAILED và throw exception
        record.setStatus(MailRecord.Status.FAILED);
        repo.save(record);
        log.error("[THẤT BẠI] Đã thử {} lần, dừng gửi cho: {}", maxAttempts, e.getEmail());
        throw new MailSendException(e.getEmail(), maxAttempts, lastException);
    }
}
