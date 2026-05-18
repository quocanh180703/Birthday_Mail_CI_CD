package com.example.reader.service;

import com.example.reader.config.RabbitConfig;
import com.example.reader.model.Employee;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PublisherService Tests")
public class PublisherServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private PublisherService publisherService;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        publisherService = new PublisherService(rabbitTemplate, objectMapper);
    }

    @Test
    @DisplayName("Should publish all employees to queue")
    public void testPublishAll_Success() throws Exception {
        // Arrange
        Employee emp1 = new Employee("João Silva", "joao@example.com", LocalDate.of(1990, 5, 15), null);
        Employee emp2 = new Employee("Maria Santos", "maria@example.com", LocalDate.of(1985, 3, 20), "/path/image.jpg");
        List<Employee> employees = Arrays.asList(emp1, emp2);

        // Act
        publisherService.publishAll(employees);

        // Assert
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(rabbitTemplate, times(2)).convertAndSend(routingKeyCaptor.capture(), messageCaptor.capture());

        assertThat(routingKeyCaptor.getAllValues()).allMatch(key -> key.equals(RabbitConfig.QUEUE));
        assertThat(messageCaptor.getAllValues()).hasSize(2);
    }

    @Test
    @DisplayName("Should handle empty employee list")
    public void testPublishAll_EmptyList() {
        // Arrange
        List<Employee> employees = Collections.emptyList();

        // Act
        publisherService.publishAll(employees);

        // Assert
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString());
    }

    @Test
    @DisplayName("Should publish with correct JSON format")
    public void testPublishAll_JsonFormat() throws Exception {
        // Arrange
        Employee emp = new Employee("João Silva", "joao@example.com", LocalDate.of(1990, 5, 15), null);
        List<Employee> employees = Collections.singletonList(emp);

        // Act
        publisherService.publishAll(employees);

        // Assert
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(eq(RabbitConfig.QUEUE), messageCaptor.capture());

        String json = messageCaptor.getValue();
        Employee deserializedEmp = objectMapper.readValue(json, Employee.class);

        assertThat(deserializedEmp.getName()).isEqualTo("João Silva");
        assertThat(deserializedEmp.getEmail()).isEqualTo("joao@example.com");
        assertThat(deserializedEmp.getDob()).isEqualTo(LocalDate.of(1990, 5, 15));
    }

    @Test
    @DisplayName("Should throw exception when RabbitTemplate fails")
    public void testPublishAll_FailureThrowsException() {
        // Arrange
        Employee emp = new Employee("João Silva", "joao@example.com", LocalDate.of(1990, 5, 15), null);
        List<Employee> employees = Collections.singletonList(emp);

        doThrow(new RuntimeException("AMQP Error")).when(rabbitTemplate)
                .convertAndSend(anyString(), anyString());

        // Act & Assert
        assertThatThrownBy(() -> publisherService.publishAll(employees))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should handle multiple employees correctly")
    public void testPublishAll_MultipleEmployees() throws Exception {
        // Arrange
        List<Employee> employees = Arrays.asList(
                new Employee("João", "joao@example.com", LocalDate.of(1990, 5, 15), null),
                new Employee("Maria", "maria@example.com", LocalDate.of(1985, 3, 20), "/path/image.jpg"),
                new Employee("Pedro", "pedro@example.com", LocalDate.of(1992, 7, 10), null),
                new Employee("Ana", "ana@example.com", LocalDate.of(1995, 12, 25), "/path/photo.jpg")
        );

        // Act
        publisherService.publishAll(employees);

        // Assert
        verify(rabbitTemplate, times(4)).convertAndSend(eq(RabbitConfig.QUEUE), anyString());
    }
}
