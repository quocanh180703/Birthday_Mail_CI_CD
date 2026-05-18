package com.example.mailer.service;

import com.example.mailer.entity.MailRecord;
import com.example.mailer.model.Employee;
import com.example.mailer.repository.MailRecordRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.time.LocalDateTime;

@Service
public class MailService {
    private final JavaMailSender mailSender;
    private final MailRecordRepository repo;
    private final TemplateEngine templateEngine;

    public MailService(JavaMailSender mailSender, MailRecordRepository repo, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.repo = repo;
        this.templateEngine = templateEngine;
    }

    @Transactional
    public void sendBirthdayEmail(Employee e) {
        System.out.println(">>> [Bắt đầu] Quy trình gửi mail cho: " + e.getEmail());

        // 1. Chuẩn bị Template và dữ liệu
        Context context = new Context();
        context.setVariable("name", e.getName());
        boolean hasImage = e.getImagePath() != null && !e.getImagePath().isBlank() && new File(e.getImagePath()).exists();
        context.setVariable("hasImage", hasImage);

        String htmlContent = templateEngine.process("birthday-email", context);

        // 2. Lưu bản ghi PENDING vào DB
        MailRecord record = new MailRecord();
        try {
            record.setRecipient(e.getEmail());
            record.setSubject("Happy Birthday " + e.getName() + "! 🎉");
            record.setBody(htmlContent); // Lưu toàn bộ HTML đã render vào DB
            record.setAttempts(0);
            record.setStatus(MailRecord.Status.PENDING);
            record = repo.save(record);
            System.out.println(">>> Đã lưu bản ghi PENDING vào DB. ID: " + record.getId());
        } catch (Exception dbEx) {
            System.err.println("!!! LỖI DATABASE: " + dbEx.getMessage());
            return;
        }

        // 3. Logic gửi mail với retry
        int maxAttempts = 3;
        long backoff = 2000L;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                System.out.println(">>> Đang thử gửi mail lần " + attempt + "...");

                MimeMessage msg = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

                helper.setTo(e.getEmail());
                helper.setSubject(record.getSubject());
                helper.setText(htmlContent, true); // true = gửi định dạng HTML

                // Đính kèm ảnh inline nếu tồn tại
                if (hasImage) {
                    FileSystemResource res = new FileSystemResource(new File(e.getImagePath()));
                    helper.addInline("birthday_img", res);
                }

                mailSender.send(msg);

                // Lưu trạng thái thành công
                record.setAttempts(attempt);
                record.setStatus(MailRecord.Status.SENT);
                record.setSentAt(LocalDateTime.now());
                record.setLastError(null);
                repo.save(record);

                System.out.println(">>> [THÀNH CÔNG] Đã gửi mail cho: " + e.getEmail());
                return;

            } catch (Exception ex) {
                System.err.println("!!! Thất bại lần " + attempt + ": " + ex.getMessage());
                record.setAttempts(attempt);
                record.setLastError(ex.getMessage());
                repo.save(record);

                if (attempt < maxAttempts) {
                    try { Thread.sleep(backoff); } catch (InterruptedException ignored) {}
                    backoff *= 2;
                } else {
                    record.setStatus(MailRecord.Status.FAILED);
                    repo.save(record);
                    System.err.println("!!! [THẤT BẠI] Dừng sau 3 lần thử.");
                }
            }
        }
    }
}