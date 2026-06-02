package com.example.mailer.controller;

import com.example.mailer.entity.MailRecord;
import com.example.mailer.repository.MailRecordRepository;
import org.junit.jupiter.api.BeforeEach;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.rabbitmq.listener.simple.auto-startup=false"
})
public class MailRecordControllerIntegrationTest {

    @MockBean
    private JavaMailSender javaMailSender;

    @MockBean
    private ConnectionFactory connectionFactory;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MailRecordRepository repo;

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