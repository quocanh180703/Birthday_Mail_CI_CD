package com.example.mailer.service;

import com.example.mailer.entity.MailRecord;
import com.example.mailer.repository.MailRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class MailRecordServiceTest {
    @Mock
    MailRecordRepository repo;

    @InjectMocks
    MailRecordService service;

    @Test
    void getRecentByStatusDto_returnsMappedList() {
        MailRecord r = new MailRecord();
        r.setId(1L);
        r.setRecipient("a@x.com");
        r.setSubject("Hi");
        r.setCreatedAt(LocalDateTime.of(2026,6,1,8,0));
        r.setStatus(MailRecord.Status.SENT);

        doReturn(List.of(r)).when(repo).findRecentByStatusNative("SENT", PageRequest.of(0, 10));

        var dtos = service.getRecentByStatusDto("SENT", 10);
        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).recipient()).isEqualTo("a@x.com");
    }
}
