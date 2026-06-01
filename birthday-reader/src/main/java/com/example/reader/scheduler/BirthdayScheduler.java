package com.example.reader.scheduler;

import com.example.reader.service.BirthdayJobService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BirthdayScheduler {
    private final BirthdayJobService birthdayJobService;

    public BirthdayScheduler(BirthdayJobService birthdayJobService) {
        this.birthdayJobService = birthdayJobService;
    }

    @Scheduled(cron = "${app.scheduler.cron:0 0 8 * * *}", zone = "${app.scheduler.timezone:Asia/Ho_Chi_Minh}")
    public void runEveryMorning() {
        triggerJob();
    }

    public int triggerJob() {
        return birthdayJobService.execute();
    }
}
