package me.ezzedine.mohammed.openexchangerates4j;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Slf4j
class OpenExchangeRatesCurrencyRatesManager {

    private final OpenExchangeRatesClient client;
    private final OpenExchangeRatesCurrencyRatesCacheManager cacheManager;
    private final OpenExchangeRatesCurrencyRatesFailureTracker failureTracker;
    private final OpenExchangeRatesDateFactory openExchangeRatesDateFactory;
    private final OpenExchangeRatesConfiguration configuration;

    public OpenExchangeRatesCurrencyRatesManager(OpenExchangeRatesClient client,
                                                  OpenExchangeRatesCurrencyRatesCacheManagerFactory cacheManagerFactory,
                                                  OpenExchangeRatesCurrencyRatesFailureTrackerFactory failureTrackerFactory,
                                                  OpenExchangeRatesDateFactory openExchangeRatesDateFactory,
                                                  OpenExchangeRatesConfiguration configuration) {
        this.client = client;
        this.cacheManager = cacheManagerFactory.get();
        this.failureTracker = failureTrackerFactory.get();
        this.openExchangeRatesDateFactory = openExchangeRatesDateFactory;
        this.configuration = configuration;
    }


    public OpenExchangeRatesCurrencyRates getRates() {
        Optional<OpenExchangeRatesCurrencyRatesCache> cachedRates = getCachedRates();

        if (cachedRates.isPresent() && cacheIsStillActive(cachedRates.get())) {
            log.info("Currency rates cache is still active, so it will be used.");
            return toRates(cachedRates.get(), false);
        }

        if (isInRetryBackoffWindow()) {
            log.info("A previous fetch attempt failed recently, so the server will not be retried yet.");
            return degradedResult(cachedRates);
        }

        try {
            log.info("Fetching the currency rates from the server");
            OpenExchangeRatesCurrencyRatesApiResponse response = client.fetchCurrencyRates();
            Date now = openExchangeRatesDateFactory.now();

            if (configuration.getCache().isEnabled()) {
                saveRatesInCache(response.base(), response.rates(), now);
            }
            clearFailure();

            return OpenExchangeRatesCurrencyRates.builder()
                    .base(response.base())
                    .rates(response.rates())
                    .lastUpdatedAt(now)
                    .stale(false)
                    .build();
        } catch (RuntimeException e) {
            log.error("Failed to fetch the currency rates from the server", e);
            recordFailure();
            return degradedResult(cachedRates);
        }
    }

    private OpenExchangeRatesCurrencyRates degradedResult(Optional<OpenExchangeRatesCurrencyRatesCache> cachedRates) {
        return cachedRates.map(cache -> toRates(cache, true))
                .orElseThrow(OpenExchangeRatesUnavailableException::new);
    }

    private OpenExchangeRatesCurrencyRates toRates(OpenExchangeRatesCurrencyRatesCache cache, boolean stale) {
        return OpenExchangeRatesCurrencyRates.builder()
                .base(cache.getBase())
                .rates(cache.getRates())
                .lastUpdatedAt(cache.getLastUpdatedAt())
                .stale(stale)
                .build();
    }

    private Optional<OpenExchangeRatesCurrencyRatesCache> getCachedRates() {
        if (!configuration.getCache().isEnabled()) {
            return Optional.empty();
        }
        return cacheManager.fetchCache();
    }

    private void saveRatesInCache(String base, Map<String, Double> rates, Date now) {
        try {
            log.info("Saving the currency rates into the cache");
            cacheManager.saveCache(OpenExchangeRatesCurrencyRatesCache.builder()
                    .base(base)
                    .rates(rates)
                    .lastUpdatedAt(now)
                    .build());
        } catch (IOException e) {
            log.error("An error happened while saving the currency exchange rates into the cache", e);
        }
    }

    private boolean cacheIsStillActive(OpenExchangeRatesCurrencyRatesCache cachedRates) {
        return cachedRates.getLastUpdatedAt().toInstant().until(openExchangeRatesDateFactory.now().toInstant(), ChronoUnit.MINUTES) <= configuration.getCache().getLifespan();
    }

    private boolean isInRetryBackoffWindow() {
        return failureTracker.fetchFailure()
                .map(failure -> failure.getLastFailedAt().toInstant().until(openExchangeRatesDateFactory.now().toInstant(), ChronoUnit.MINUTES) < configuration.getCache().getRetry().getBackoff())
                .orElse(false);
    }

    private void recordFailure() {
        try {
            failureTracker.recordFailure(OpenExchangeRatesCurrencyRatesFailure.builder()
                    .lastFailedAt(openExchangeRatesDateFactory.now())
                    .build());
        } catch (IOException e) {
            log.error("An error happened while recording the failed fetch attempt", e);
        }
    }

    private void clearFailure() {
        try {
            failureTracker.clearFailure();
        } catch (IOException e) {
            log.error("An error happened while clearing the previously recorded failed fetch attempt", e);
        }
    }

}
