package com.example.mailer.controller;

import com.example.mailer.entity.MailRecord;
import com.example.mailer.repository.MailRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase(replace = org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.ANY)
@org.springframework.test.context.TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.rabbitmq.host=localhost",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration"
})
public class MailRecordControllerIntegrationTest {

    @TestConfiguration
    static class MailSenderMockConfig {
        @Bean
        public JavaMailSender javaMailSender() {
            return mock(JavaMailSender.class);
        }
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    MailRecordRepository repo;

    @BeforeEach
    void setup() {
        repo.deleteAll();
        MailRecord a = new MailRecord();
        a.setRecipient("one@x.com");
        a.setSubject("S1");
        a.setCreatedAt(LocalDateTime.now().minusDays(1));
        a.setStatus(MailRecord.Status.SENT);

        MailRecord b = new MailRecord();
        b.setRecipient("two@x.com");
        b.setSubject("S2");
        b.setCreatedAt(LocalDateTime.now());
        b.setStatus(MailRecord.Status.SENT);

        MailRecord c = new MailRecord();
        c.setRecipient("three@x.com");
        c.setSubject("S3");
        c.setCreatedAt(LocalDateTime.now());
        c.setStatus(MailRecord.Status.FAILED);

        repo.save(a);
        repo.save(b);
        repo.save(c);
    }

    @Test
    void recentByStatus_returnsOnlySent_limitApplied() throws Exception {
        mvc.perform(get("/api/mail-records/recent-by-status")
                        .param("status", "SENT")
                        .param("limit", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
