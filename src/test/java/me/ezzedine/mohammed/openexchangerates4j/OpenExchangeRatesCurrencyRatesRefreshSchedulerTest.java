package me.ezzedine.mohammed.openexchangerates4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpenExchangeRatesCurrencyRatesRefreshSchedulerTest {

    private OpenExchangeRatesCurrencyRatesManager ratesManager;
    private ScheduledExecutorService scheduledExecutorService;
    private int lifespan;
    private Runnable scheduledTask;

    @BeforeEach
    void setUp() {
        ratesManager = mock(OpenExchangeRatesCurrencyRatesManager.class);
        scheduledExecutorService = mock(ScheduledExecutorService.class);

        OpenExchangeRatesConfiguration configuration = mock(OpenExchangeRatesConfiguration.class);
        OpenExchangeRatesConfiguration.CacheConfiguration cacheConfiguration = mock(OpenExchangeRatesConfiguration.CacheConfiguration.class);
        lifespan = new Random().nextInt(100, 300);
        when(cacheConfiguration.getLifespan()).thenReturn(lifespan);
        when(configuration.getCache()).thenReturn(cacheConfiguration);

        new OpenExchangeRatesCurrencyRatesRefreshScheduler(ratesManager, configuration, scheduledExecutorService);

        ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduledExecutorService).scheduleAtFixedRate(taskCaptor.capture(), eq(0L), eq((long) lifespan), eq(TimeUnit.MINUTES));
        scheduledTask = taskCaptor.getValue();
    }

    @Test
    @DisplayName("it should fetch the rates when the scheduled task runs")
    void it_should_fetch_the_rates_when_the_scheduled_task_runs() {
        scheduledTask.run();

        verify(ratesManager).getRates();
    }

    @Test
    @DisplayName("it should not throw an exception when the scheduled task fails")
    void it_should_not_throw_an_exception_when_the_scheduled_task_fails() {
        when(ratesManager.getRates()).thenThrow(new RuntimeException("server unavailable"));

        assertDoesNotThrow(() -> scheduledTask.run());
    }

    @Test
    @DisplayName("it should shut down the underlying executor when shutdown is called")
    void it_should_shut_down_the_underlying_executor_when_shutdown_is_called() {
        new OpenExchangeRatesCurrencyRatesRefreshScheduler(ratesManager, mockConfiguration(), scheduledExecutorService).shutdown();

        verify(scheduledExecutorService, org.mockito.Mockito.atLeastOnce()).shutdown();
    }

    private OpenExchangeRatesConfiguration mockConfiguration() {
        OpenExchangeRatesConfiguration configuration = mock(OpenExchangeRatesConfiguration.class);
        OpenExchangeRatesConfiguration.CacheConfiguration cacheConfiguration = mock(OpenExchangeRatesConfiguration.CacheConfiguration.class);
        when(cacheConfiguration.getLifespan()).thenReturn(lifespan);
        when(configuration.getCache()).thenReturn(cacheConfiguration);
        return configuration;
    }
}
