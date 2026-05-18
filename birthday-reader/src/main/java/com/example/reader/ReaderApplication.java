package com.example.reader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReaderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReaderApplication.class, args);
    }
}
