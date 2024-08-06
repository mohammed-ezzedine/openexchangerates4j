package me.ezzedine.mohammed.openexchangerates4j;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

@Slf4j
class OpenExchangeRatesCurrencyRatesManager {

    private final OpenExchangeRatesClient client;
    private final OpenExchangeRatesCurrencyRatesCacheManager cacheManager;
    private final OpenExchangeRatesDateFactory openExchangeRatesDateFactory;
    private final OpenExchangeRatesConfiguration configuration;

    public OpenExchangeRatesCurrencyRatesManager(OpenExchangeRatesClient client, OpenExchangeRatesCurrencyRatesCacheManagerFactory cacheManagerFactory, OpenExchangeRatesDateFactory openExchangeRatesDateFactory, OpenExchangeRatesConfiguration configuration) {
        this.client = client;
        this.cacheManager = cacheManagerFactory.get();
        this.openExchangeRatesDateFactory = openExchangeRatesDateFactory;
        this.configuration = configuration;
    }


    @SneakyThrows
    public OpenExchangeRatesCurrencyRates getRates() {
        Optional<OpenExchangeRatesCurrencyRatesCache> cachedRates = getCachedRates();

        if (cachedRates.isPresent() && cacheIsStillActive(cachedRates.get())) {
            log.info("Currency rates cache is still active, so it will be used.");
            return OpenExchangeRatesCurrencyRates.builder()
                    .base(cachedRates.get().getBase())
                    .rates(cachedRates.get().getRates())
                    .build();
        }

        log.info("Fetching the currency rates from the server");
        OpenExchangeRatesCurrencyRatesApiResponse response = client.fetchCurrencyRates();

        if (configuration.getCache().isEnabled()) {
            saveRatesInCache(response.base(), response.rates());
        }

        return OpenExchangeRatesCurrencyRates.builder()
                .base(response.base())
                .rates(response.rates())
                .build();
    }

    private Optional<OpenExchangeRatesCurrencyRatesCache> getCachedRates() {
        if (!configuration.getCache().isEnabled()) {
            return Optional.empty();
        }
        return cacheManager.fetchCache();
    }

    private void saveRatesInCache(String base, Map<String, Double> rates) {
        try {
            log.info("Saving the currency rates into the cache");
            cacheManager.saveCache(OpenExchangeRatesCurrencyRatesCache.builder()
                    .base(base)
                    .rates(rates)
                    .lastUpdatedAt(openExchangeRatesDateFactory.now())
                    .build());
        } catch (IOException e) {
            log.error("An error happened while saving the currency exchange rates into the cache", e);
        }
    }

    private boolean cacheIsStillActive(OpenExchangeRatesCurrencyRatesCache cachedRates) {
        return cachedRates.getLastUpdatedAt().toInstant().until(openExchangeRatesDateFactory.now().toInstant(), ChronoUnit.MINUTES) <= configuration.getCache().getLifespan();
    }

}
