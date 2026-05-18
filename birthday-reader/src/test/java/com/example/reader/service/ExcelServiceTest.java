package com.example.reader.service;

import com.example.reader.model.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DisplayName("ExcelService Tests")
public class ExcelServiceTest {

    @Autowired
    private ExcelService excelService;

    @Test
    @DisplayName("Should have ExcelService bean")
    public void testExcelServiceExists() {
        assertThat(excelService).isNotNull();
    }

    @Test
    @DisplayName("Should be an instance of ExcelService")
    public void testExcelServiceInstance() {
        assertThat(excelService).isInstanceOf(ExcelService.class);
    }
}
