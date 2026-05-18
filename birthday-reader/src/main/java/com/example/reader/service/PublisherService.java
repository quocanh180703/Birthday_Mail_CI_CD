package com.example.reader.service;

import com.example.reader.config.RabbitConfig;
import com.example.reader.model.Employee;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublisherService {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper mapper;

    public PublisherService(RabbitTemplate rabbitTemplate, ObjectMapper mapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapper = mapper;
    }

    public void publishAll(List<Employee> employees) {
        try {
            for (Employee e : employees) {
                String json = mapper.writeValueAsString(e);
                rabbitTemplate.convertAndSend(RabbitConfig.QUEUE, json);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
