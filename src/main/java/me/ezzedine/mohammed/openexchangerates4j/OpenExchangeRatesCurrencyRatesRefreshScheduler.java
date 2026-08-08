package me.ezzedine.mohammed.openexchangerates4j;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
class OpenExchangeRatesCurrencyRatesRefreshScheduler {

    private final ScheduledExecutorService scheduledExecutorService;

    OpenExchangeRatesCurrencyRatesRefreshScheduler(OpenExchangeRatesCurrencyRatesManager ratesManager,
                                                     OpenExchangeRatesConfiguration configuration,
                                                     ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
        long lifespan = configuration.getCache().getLifespan();
        this.scheduledExecutorService.scheduleAtFixedRate(() -> refresh(ratesManager), 0, lifespan, TimeUnit.MINUTES);
    }

    void shutdown() {
        scheduledExecutorService.shutdown();
    }

    private void refresh(OpenExchangeRatesCurrencyRatesManager ratesManager) {
        try {
            log.info("Proactively refreshing the currency rates cache");
            ratesManager.getRates();
        } catch (RuntimeException e) {
            log.warn("Proactive background refresh of the currency rates failed", e);
        }
    }
}
