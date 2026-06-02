package com.example.reader.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BirthdayScheduler Tests")
public class BirthdaySchedulerTest {

    @Mock
    private com.example.reader.service.BirthdayJobService birthdayJobService;

    private BirthdayScheduler birthdayScheduler;

    @Test
    @DisplayName("Should delegate manual trigger to shared job service")
    public void testTriggerJob_DelegatesToJobService() {
        birthdayScheduler = new BirthdayScheduler(birthdayJobService);

        when(birthdayJobService.execute()).thenReturn(3);

        int result = birthdayScheduler.triggerJob();

        assertThat(result).isEqualTo(3);
        verify(birthdayJobService).execute();
    }

    @Test
    @DisplayName("Should trigger scheduled run through the same job service")
    public void testRunEveryMorning_DelegatesToJobService() {
        birthdayScheduler = new BirthdayScheduler(birthdayJobService);

        birthdayScheduler.runEveryMorning();

        verify(birthdayJobService).execute();
    }
}
