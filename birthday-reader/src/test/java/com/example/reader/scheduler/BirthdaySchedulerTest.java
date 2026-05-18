package com.example.reader.scheduler;

import com.example.reader.model.Employee;
import com.example.reader.service.ExcelService;
import com.example.reader.service.PublisherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BirthdayScheduler Tests")
public class BirthdaySchedulerTest {

    @Mock
    private ExcelService excelService;

    @Mock
    private PublisherService publisherService;

    private BirthdayScheduler birthdayScheduler;
    private Path tempFile;

    @BeforeEach
    public void setUp() throws Exception {
        tempFile = Files.createTempFile("test-employees", ".xlsx");
        birthdayScheduler = new BirthdayScheduler(excelService, publisherService, tempFile.toString());
    }

    @Test
    @DisplayName("Should trigger job and publish employees")
    public void testTriggerJob_Success() throws Exception {
        // Arrange
        List<Employee> mockEmployees = Arrays.asList(
                new Employee("João Silva", "joao@example.com", LocalDate.of(1990, 5, 15), null),
                new Employee("Maria Santos", "maria@example.com", LocalDate.of(1985, 3, 20), null)
        );

        when(excelService.readFromStream(any())).thenReturn(mockEmployees);

        // Act
        int result = birthdayScheduler.triggerJob();

        // Assert
        assertThat(result).isEqualTo(2);
        verify(excelService).readFromStream(any());
        verify(publisherService).publishAll(mockEmployees);
    }

    @Test
    @DisplayName("Should return 0 when file does not exist")
    public void testTriggerJob_FileNotFound() throws Exception {
        // Arrange
        BirthdayScheduler scheduler = new BirthdayScheduler(excelService, publisherService, "/non/existent/file.xlsx");

        // Act
        int result = scheduler.triggerJob();

        // Assert
        assertThat(result).isEqualTo(0);
        verify(excelService, never()).readFromStream(any());
        verify(publisherService, never()).publishAll(any());
    }

    @Test
    @DisplayName("Should return 0 and handle exception when reading file fails")
    public void testTriggerJob_ExceptionHandling() throws Exception {
        // Arrange
        when(excelService.readFromStream(any())).thenThrow(new RuntimeException("Failed to read Excel"));

        // Act
        int result = birthdayScheduler.triggerJob();

        // Assert
        assertThat(result).isEqualTo(0);
        verify(publisherService, never()).publishAll(any());
    }

    @Test
    @DisplayName("Should return empty list when no employees found")
    public void testTriggerJob_NoEmployees() throws Exception {
        // Arrange
        when(excelService.readFromStream(any())).thenReturn(Arrays.asList());

        // Act
        int result = birthdayScheduler.triggerJob();

        // Assert
        assertThat(result).isEqualTo(0);
        verify(publisherService).publishAll(Arrays.asList());
    }

    @Test
    @DisplayName("Should return correct count of published employees")
    public void testTriggerJob_CorrectCount() throws Exception {
        // Arrange
        List<Employee> mockEmployees = Arrays.asList(
                new Employee("João", "joao@example.com", LocalDate.of(1990, 5, 15), null),
                new Employee("Maria", "maria@example.com", LocalDate.of(1985, 3, 20), null),
                new Employee("Pedro", "pedro@example.com", LocalDate.of(1992, 7, 10), null),
                new Employee("Ana", "ana@example.com", LocalDate.of(1995, 12, 25), null),
                new Employee("Carlos", "carlos@example.com", LocalDate.of(1988, 2, 14), null)
        );

        when(excelService.readFromStream(any())).thenReturn(mockEmployees);

        // Act
        int result = birthdayScheduler.triggerJob();

        // Assert
        assertThat(result).isEqualTo(5);
        verify(publisherService).publishAll(mockEmployees);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (tempFile != null && Files.exists(tempFile)) {
            Files.delete(tempFile);
        }
    }
}
