package com.example.mailer.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String QUEUE = "birthday.employees";

    @Bean
    public Queue birthdayQueue() {
        return new Queue(QUEUE, true);
    }
}
