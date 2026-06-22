package com.example.mailer.util;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailUtils Tests")
class EmailUtilsTest {

    @Mock JavaMailSender mailSender;

    // ─── hasValidImage ────────────────────────────────────────────────────────

    @Test
    @DisplayName("hasValidImage — null → false")
    void hasValidImage_null_returnsFalse() {
        assertThat(EmailUtils.hasValidImage(null)).isFalse();
    }

    @Test
    @DisplayName("hasValidImage — blank → false")
    void hasValidImage_blank_returnsFalse() {
        assertThat(EmailUtils.hasValidImage("   ")).isFalse();
    }

    @Test
    @DisplayName("hasValidImage — đường dẫn không tồn tại → false")
    void hasValidImage_nonExistentPath_returnsFalse() {
        assertThat(EmailUtils.hasValidImage("/path/that/does/not/exist.png")).isFalse();
    }

    @Test
    @DisplayName("hasValidImage — file tồn tại → true")
    void hasValidImage_existingFile_returnsTrue(@TempDir Path tempDir) throws Exception {
        File img = tempDir.resolve("photo.png").toFile();
        img.createNewFile();
        assertThat(EmailUtils.hasValidImage(img.getAbsolutePath())).isTrue();
    }

    // ─── buildHtmlMessage ─────────────────────────────────────────────────────

    @Test
    @DisplayName("buildHtmlMessage — không có ảnh — tạo MimeMessage thành công")
    void buildHtmlMessage_withoutImage_createsMimeMessage() throws Exception {
        // Dùng session thật của JavaMail để MimeMessageHelper có thể setTo/setSubject
        jakarta.mail.Session session = jakarta.mail.Session.getDefaultInstance(new java.util.Properties());
        MimeMessage realMsg = new MimeMessage(session);
        
        // CHỈ GIỮ LẠI DÒNG MOCK NÀY:
        when(mailSender.createMimeMessage()).thenReturn(realMsg);

        MimeMessage result = EmailUtils.buildHtmlMessage(
                mailSender,
                "alice@example.com",
                "Happy Birthday!",
                "<html>Hi Alice</html>",
                null
        );

        assertThat(result).isNotNull();
        assertThat(result.getAllRecipients()).isNotNull();
    }

    @Test
    @DisplayName("buildHtmlMessage — có ảnh hợp lệ — không throw exception")
    void buildHtmlMessage_withValidImage_doesNotThrow(@TempDir Path tempDir) throws Exception {
        File img = tempDir.resolve("photo.png").toFile();
        img.createNewFile();

        jakarta.mail.Session session = jakarta.mail.Session.getDefaultInstance(new java.util.Properties());
        MimeMessage realMsg = new MimeMessage(session);
        when(mailSender.createMimeMessage()).thenReturn(realMsg);

        assertThatCode(() -> EmailUtils.buildHtmlMessage(
                mailSender,
                "bob@example.com",
                "Happy Birthday!",
                "<html>Hi Bob</html>",
                img.getAbsolutePath()
        )).doesNotThrowAnyException();
    }
}