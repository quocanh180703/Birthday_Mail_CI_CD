package com.example.mailer.controller;

import com.example.mailer.service.MailRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mail-records")
public class MailRecordController {
    private final MailRecordService service;

    public MailRecordController(MailRecordService service) {
        this.service = service;
    }

    @GetMapping("/recent-by-status")
    public ResponseEntity<?> recentByStatus(@RequestParam String status, @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(service.getRecentByStatusDto(status.toUpperCase(), limit));
    }
}
