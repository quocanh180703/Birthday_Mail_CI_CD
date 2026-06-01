package com.example.reader.service;

import com.example.reader.model.Employee;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BirthdayJobService Tests")
public class BirthdayJobServiceTest {

    @Mock
    private ExcelService excelService;

    @Mock
    private PublisherService publisherService;

    private BirthdayJobService birthdayJobService;
    private Path tempFile;

    @BeforeEach
    public void setUp() throws Exception {
        tempFile = Files.createTempFile("test-employees", ".xlsx");
        birthdayJobService = new BirthdayJobService(excelService, publisherService, tempFile.toString());
    }

    @Test
    @DisplayName("Should execute job and publish employees")
    public void testExecute_Success() throws Exception {
        List<Employee> mockEmployees = Arrays.asList(
                new Employee("João Silva", "joao@example.com", LocalDate.of(1990, 5, 15), null),
                new Employee("Maria Santos", "maria@example.com", LocalDate.of(1985, 3, 20), null)
        );

        doReturn(mockEmployees).when(excelService).readFromStream(any());

        int result = birthdayJobService.execute();

        assertThat(result).isEqualTo(2);
        verify(excelService).readFromStream(any());
        verify(publisherService).publishAll(mockEmployees);
    }

    @Test
    @DisplayName("Should return 0 when file does not exist")
    public void testExecute_FileNotFound() throws Exception {
        BirthdayJobService service = new BirthdayJobService(excelService, publisherService, "/non/existent/file.xlsx");

        int result = service.execute();

        assertThat(result).isEqualTo(0);
        verify(excelService, never()).readFromStream(any());
        verify(publisherService, never()).publishAll(any());
    }

    @Test
    @DisplayName("Should return 0 and handle exception when reading file fails")
    public void testExecute_ExceptionHandling() throws Exception {
        doThrow(new RuntimeException("Failed to read Excel")).when(excelService).readFromStream(any());

        int result = birthdayJobService.execute();

        assertThat(result).isEqualTo(0);
        verify(publisherService, never()).publishAll(any());
    }

    @Test
    @DisplayName("Should return 0 when no employees found")
    public void testExecute_NoEmployees() throws Exception {
        doReturn(Arrays.asList()).when(excelService).readFromStream(any());

        int result = birthdayJobService.execute();

        assertThat(result).isEqualTo(0);
        verify(publisherService).publishAll(Arrays.asList());
    }

    @Test
    @DisplayName("Should return correct count of published employees")
    public void testExecute_CorrectCount() throws Exception {
        List<Employee> mockEmployees = Arrays.asList(
                new Employee("João", "joao@example.com", LocalDate.of(1990, 5, 15), null),
                new Employee("Maria", "maria@example.com", LocalDate.of(1985, 3, 20), null),
                new Employee("Pedro", "pedro@example.com", LocalDate.of(1992, 7, 10), null),
                new Employee("Ana", "ana@example.com", LocalDate.of(1995, 12, 25), null),
                new Employee("Carlos", "carlos@example.com", LocalDate.of(1988, 2, 14), null)
        );

        doReturn(mockEmployees).when(excelService).readFromStream(any());

        int result = birthdayJobService.execute();

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