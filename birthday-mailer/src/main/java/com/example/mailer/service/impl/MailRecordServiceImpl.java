package com.example.mailer.service.impl;

import com.example.mailer.dto.MailRecordDto;
import com.example.mailer.entity.MailRecord;
import com.example.mailer.repository.MailRecordRepository;
import com.example.mailer.service.IMailRecordService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MailRecordServiceImpl implements IMailRecordService {

    private final MailRecordRepository repo;

    public MailRecordServiceImpl(MailRecordRepository repo) {
        this.repo = repo;
    }

    // ─── Query đơn giản theo status ───────────────────────────────────────────

    @Override
    public List<MailRecord> getRecentByStatus(String status, int limit) {
        return repo.findRecentByStatusNative(status, PageRequest.of(0, limit));
    }

    @Override
    public List<MailRecordDto> getRecentByStatusDto(String status, int limit) {
        return getRecentByStatus(status, limit).stream()
                .map(r -> new MailRecordDto(
                        r.getId(),
                        r.getRecipient(),
                        r.getSubject(),
                        r.getCreatedAt(),
                        r.getStatus().name()))
                .collect(Collectors.toList());
    }
}
