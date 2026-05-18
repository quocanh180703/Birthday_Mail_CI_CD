package com.example.reader.controller;

import com.example.reader.scheduler.BirthdayScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scheduler")
public class SchedulerController {
    private final BirthdayScheduler birthdayScheduler;

    public SchedulerController(BirthdayScheduler birthdayScheduler) {
        this.birthdayScheduler = birthdayScheduler;
    }

    @PostMapping("/run")
    public ResponseEntity<String> runManually() {
        int published = birthdayScheduler.triggerJob();
        return ResponseEntity.ok("Manual scheduler run completed. Published " + published + " messages.");
    }
}
