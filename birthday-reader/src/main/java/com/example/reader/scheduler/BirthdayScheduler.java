package com.example.reader.scheduler;

import com.example.reader.service.ExcelService;
import com.example.reader.service.PublisherService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

@Component
public class BirthdayScheduler {
    private final ExcelService excelService;
    private final PublisherService publisherService;
    private final String excelPath;

    public BirthdayScheduler(
            ExcelService excelService,
            PublisherService publisherService,
            @Value("${app.excel.path:data/employees.xlsx}") String excelPath) {
        this.excelService = excelService;
        this.publisherService = publisherService;
        this.excelPath = excelPath;
    }

    @Scheduled(cron = "${app.scheduler.cron:0 0 8 * * *}", zone = "${app.scheduler.timezone:Asia/Ho_Chi_Minh}")
    public void runEveryMorning() {
        triggerJob();
    }

    public int triggerJob() {
        File file = new File(excelPath);
        if (!file.exists()) {
            return 0;
        }

        try (FileInputStream in = new FileInputStream(file)) {
            List<?> employees = excelService.readFromStream(in);
            publisherService.publishAll((List) employees);
            return employees.size();
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }
}
