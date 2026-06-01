package com.example.mailer.service;

import com.example.mailer.entity.MailRecord;
import com.example.mailer.repository.MailRecordRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MailRecordService {
    private final MailRecordRepository repo;

    public MailRecordService(MailRecordRepository repo) {
        this.repo = repo;
    }

    public List<MailRecord> getRecentByStatus(String status, int limit) {
        return repo.findRecentByStatusNative(status, PageRequest.of(0, limit));
    }

    public List<MailRecordDto> getRecentByStatusDto(String status, int limit) {
        return getRecentByStatus(status, limit).stream()
                .map(r -> new MailRecordDto(r.getId(), r.getRecipient(), r.getSubject(), r.getCreatedAt(), r.getStatus().name()))
                .collect(Collectors.toList());
    }

    public record MailRecordDto(Long id, String recipient, String subject, java.time.LocalDateTime createdAt, String status) {}
}
