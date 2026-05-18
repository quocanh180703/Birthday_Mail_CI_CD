package com.example.reader.controller;

import com.example.reader.model.Employee;
import com.example.reader.service.ExcelService;
import com.example.reader.service.PublisherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UploadController.class)
@DisplayName("UploadController Tests")
public class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExcelService excelService;

    @MockBean
    private PublisherService publisherService;

    @Test
    @DisplayName("Should upload Excel file successfully")
    public void testUpload_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "employees.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "mock file content".getBytes()
        );

        java.util.List<Employee> employees = Arrays.asList(
                new Employee("João Silva", "joao@example.com", LocalDate.of(1990, 5, 15), null),
                new Employee("Maria Santos", "maria@example.com", LocalDate.of(1985, 3, 20), null)
        );

        when(excelService.readFromStream(any())).thenReturn(employees);

        // Act & Assert
        mockMvc.perform(multipart("/api/upload")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Published 2 messages"));

        verify(excelService).readFromStream(any());
        verify(publisherService).publishAll(employees);
    }

    @Test
    @DisplayName("Should handle empty employee list")
    public void testUpload_EmptyFile() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "mock file content".getBytes()
        );

        when(excelService.readFromStream(any())).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(multipart("/api/upload")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Published 0 messages"));

        verify(publisherService).publishAll(Collections.emptyList());
    }

    @Test
    @DisplayName("Should return 400 when no file provided")
    public void testUpload_NoFile() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/api/upload"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should publish multiple employees from file")
    public void testUpload_MultipleEmployees() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "employees.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "mock file content".getBytes()
        );

        java.util.List<Employee> employees = Arrays.asList(
                new Employee("João", "joao@example.com", LocalDate.of(1990, 5, 15), null),
                new Employee("Maria", "maria@example.com", LocalDate.of(1985, 3, 20), null),
                new Employee("Pedro", "pedro@example.com", LocalDate.of(1992, 7, 10), null),
                new Employee("Ana", "ana@example.com", LocalDate.of(1995, 12, 25), null),
                new Employee("Carlos", "carlos@example.com", LocalDate.of(1988, 2, 14), null)
        );

        when(excelService.readFromStream(any())).thenReturn(employees);

        // Act & Assert
        mockMvc.perform(multipart("/api/upload")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Published 5 messages"));

        verify(publisherService).publishAll(employees);
    }
}
