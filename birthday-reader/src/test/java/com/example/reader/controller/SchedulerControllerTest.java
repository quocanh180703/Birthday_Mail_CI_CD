package com.example.reader.controller;

import com.example.reader.scheduler.BirthdayScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SchedulerController.class)
@DisplayName("SchedulerController Tests")
public class SchedulerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BirthdayScheduler birthdayScheduler;

    @Test
    @DisplayName("Should run scheduler manually and return structured JSON")
    public void testRunManually_Success() throws Exception {
        // Arrange
        when(birthdayScheduler.triggerJob()).thenReturn(3);

        // Act & Assert
        mockMvc.perform(post("/api/scheduler/run"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Manual trigger completed"))
                .andExpect(jsonPath("$.publishedCount").value(3))
                .andExpect(jsonPath("$.triggeredAt").exists());

        verify(birthdayScheduler).triggerJob();
    }

    @Test
    @DisplayName("Should return 0 published messages when no employees found")
    public void testRunManually_NoEmployees() throws Exception {
        // Arrange
        when(birthdayScheduler.triggerJob()).thenReturn(0);

        // Act & Assert
        mockMvc.perform(post("/api/scheduler/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publishedCount").value(0));

        verify(birthdayScheduler).triggerJob();
    }

    @Test
    @DisplayName("Should trigger scheduler job")
    public void testRunManually_TriggerJob() throws Exception {
        // Arrange
        when(birthdayScheduler.triggerJob()).thenReturn(5);

        // Act & Assert
        mockMvc.perform(post("/api/scheduler/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(birthdayScheduler, times(1)).triggerJob();
    }

    @Test
    @DisplayName("Should return response entity with correct content type")
    public void testRunManually_ContentType() throws Exception {
        // Arrange
        when(birthdayScheduler.triggerJob()).thenReturn(2);

        // Act & Assert
        mockMvc.perform(post("/api/scheduler/run"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    @DisplayName("Should handle large number of published messages")
    public void testRunManually_LargeCount() throws Exception {
        // Arrange
        when(birthdayScheduler.triggerJob()).thenReturn(1000);

        // Act & Assert
        mockMvc.perform(post("/api/scheduler/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publishedCount").value(1000));
    }
}
