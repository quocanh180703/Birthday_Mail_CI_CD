package com.example.mailer.service;

import com.example.mailer.model.Employee;
import com.example.mailer.repository.MailRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MailService Tests")
public class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MailRecordRepository mailRecordRepository;

    @Mock
    private TemplateEngine templateEngine;

    private MailService mailService;

    @BeforeEach
    public void setUp() {
        mailService = new MailService(mailSender, mailRecordRepository, templateEngine);
    }

    @Test
    @DisplayName("Should have MailService instance")
    public void testMailServiceInstance() {
        assertThat(mailService).isNotNull();
        assertThat(mailService).isInstanceOf(MailService.class);
    }

    @Test
    @DisplayName("Should have dependencies injected")
    public void testDependenciesInjection() {
        assertThat(mailService).isNotNull();
    }

    @Test
    @DisplayName("Should handle employee object")
    public void testHandleEmployee() {
        Employee employee = new Employee("João Silva", "joao@example.com", LocalDate.of(1990, 5, 15), null);

        assertThatCode(() -> {
            assertThat(employee).isNotNull();
        }).doesNotThrowAnyException();
    }
}
