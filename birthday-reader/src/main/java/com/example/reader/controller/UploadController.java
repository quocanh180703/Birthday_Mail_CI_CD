package com.example.reader.controller;

import com.example.reader.service.ExcelService;
import com.example.reader.service.PublisherService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/api")
public class UploadController {
    private final ExcelService excelService;
    private final PublisherService publisherService;

    public UploadController(ExcelService excelService, PublisherService publisherService) {
        this.excelService = excelService;
        this.publisherService = publisherService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        File tmp = Files.createTempFile("employees", ".xlsx").toFile();
        FileCopyUtils.copy(file.getInputStream(), Files.newOutputStream(tmp.toPath()));
        List<?> employees;
        try (FileInputStream in = new FileInputStream(tmp)) {
            employees = excelService.readFromStream(in);
        }
        publisherService.publishAll((List) employees);
        return ResponseEntity.ok("Published " + employees.size() + " messages");
    }
}
