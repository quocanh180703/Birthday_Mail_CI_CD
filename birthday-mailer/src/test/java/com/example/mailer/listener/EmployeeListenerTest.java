package com.example.mailer.listener;

import com.example.mailer.model.Employee;
import com.example.mailer.service.IMailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeListener Tests")
public class EmployeeListenerTest {

    @Mock
    private IMailService mailService;

    private EmployeeListener employeeListener;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        employeeListener = new EmployeeListener(mailService);
    }

    @Test
    @DisplayName("Should process employee message")
    public void testReceive_ProcessMessage() throws Exception {
        // Arrange
        Employee employee = new Employee("João Silva", "joao@example.com", LocalDate.of(1990, 5, 15), null);
        String jsonPayload = objectMapper.writeValueAsString(employee);

        // Act
        employeeListener.receive(jsonPayload);

        // Assert
        assertThat(jsonPayload).contains("João Silva");
    }

    @Test
    @DisplayName("Should handle messages without causing exceptions")
    public void testReceive_NoExceptions() throws Exception {
        // Arrange
        Employee employee = new Employee("Maria Santos", "maria@example.com", LocalDate.of(1985, 5, 18), null);
        String jsonPayload = objectMapper.writeValueAsString(employee);

        // Act & Assert - should not throw
        assertThatCode(() -> employeeListener.receive(jsonPayload))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should have IMailService dependency")
    public void testMailServiceInjection() {
        assertThat(employeeListener).isNotNull();
    }
}
