package com.example.mailer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@SpringBootApplication
@EnableRabbit
public class MailerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MailerApplication.class, args);
    }
}
