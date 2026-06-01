package com.example.reader.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

@Service
public class BirthdayJobService {
    private final ExcelService excelService;
    private final PublisherService publisherService;
    private final String excelPath;

    public BirthdayJobService(
            ExcelService excelService,
            PublisherService publisherService,
            @Value("${app.excel.path:data/employees.xlsx}") String excelPath) {
        this.excelService = excelService;
        this.publisherService = publisherService;
        this.excelPath = excelPath;
    }

    public int execute() {
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