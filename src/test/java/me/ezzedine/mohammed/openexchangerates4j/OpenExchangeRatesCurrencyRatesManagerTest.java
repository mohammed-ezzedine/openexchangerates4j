package me.ezzedine.mohammed.openexchangerates4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OpenExchangeRatesCurrencyRatesManagerTest {

    private OpenExchangeRatesCurrencyRatesManager ratesManager;
    private OpenExchangeRatesCurrencyRatesCacheManager cacheManager;

    private Date currentDate;
    private Instant currentInstant;
    private OpenExchangeRatesClient client;
    private OpenExchangeRatesConfiguration.CacheConfiguration cacheConfiguration;
    private int cacheLifespan;

    @BeforeEach
    void setUp() {
        client = mock(OpenExchangeRatesClient.class);

        OpenExchangeRatesCurrencyRatesCacheManagerFactory cacheManagerFactory = mock(OpenExchangeRatesCurrencyRatesCacheManagerFactory.class);
        cacheManager = mock(OpenExchangeRatesCurrencyRatesCacheManager.class);
        when(cacheManagerFactory.get()).thenReturn(cacheManager);


        OpenExchangeRatesDateFactory openExchangeRatesDateFactory = mock(OpenExchangeRatesDateFactory.class);
        currentDate = mock(Date.class);
        when(openExchangeRatesDateFactory.now()).thenReturn(currentDate);
        currentInstant = mock(Instant.class);
        when(currentDate.toInstant()).thenReturn(currentInstant);

        OpenExchangeRatesConfiguration configuration = mock(OpenExchangeRatesConfiguration.class);
        cacheConfiguration = mock(OpenExchangeRatesConfiguration.CacheConfiguration.class);
        when(cacheConfiguration.isEnabled()).thenReturn(true);
        cacheLifespan = new Random().nextInt(100, 300);
        when(cacheConfiguration.getLifespan()).thenReturn(cacheLifespan);
        when(configuration.getCache()).thenReturn(cacheConfiguration);

        ratesManager = new OpenExchangeRatesCurrencyRatesManager(client, cacheManagerFactory, openExchangeRatesDateFactory, configuration);
    }

    @Nested
    @DisplayName("When an active rates cache exists")
    class ActiveRatesCacheExists {

        private OpenExchangeRatesCurrencyRatesCache cachedRates;

        @BeforeEach
        void setUp() {
            Date lastUpdatedAt = mock(Date.class);
            cachedRates = OpenExchangeRatesCurrencyRatesCache.builder()
                    .base(UUID.randomUUID().toString())
                    .rates(Map.of(
                            UUID.randomUUID().toString(), new Random().nextDouble()
                    ))
                    .lastUpdatedAt(lastUpdatedAt)
                    .build();
            when(cacheManager.fetchCache()).thenReturn(Optional.of(
                    cachedRates
            ));

            Instant lastModifiedAtInstant = mock(Instant.class);
            when(lastUpdatedAt.toInstant()).thenReturn(lastModifiedAtInstant);

            when(lastModifiedAtInstant.until(currentInstant, ChronoUnit.MINUTES)).thenReturn(new Random().nextLong(1, cacheLifespan));
        }

        @Test
        @DisplayName("it should return the rates from the cache")
        void it_should_return_the_rates_from_the_cache() {
            OpenExchangeRatesCurrencyRates rates = ratesManager.getRates();

            assertEquals(cachedRates.getBase(), rates.getBase());
            assertEquals(cachedRates.getRates(), rates.getRates());
        }
    }

    @Nested
    @DisplayName("When an inactive rates cache exists")
    class InactiveRatesCacheExists {
        private OpenExchangeRatesCurrencyRatesApiResponse remoteRates;

        @BeforeEach
        void setUp() {
            Date lastUpdatedAt = mock(Date.class);
            OpenExchangeRatesCurrencyRatesCache cachedRates = OpenExchangeRatesCurrencyRatesCache.builder()
                    .base(UUID.randomUUID().toString())
                    .rates(Map.of(
                            UUID.randomUUID().toString(), new Random().nextDouble()
                    ))
                    .lastUpdatedAt(lastUpdatedAt)
                    .build();
            when(cacheManager.fetchCache()).thenReturn(Optional.of(
                    cachedRates
            ));

            Instant lastModifiedAtInstant = mock(Instant.class);
            when(lastUpdatedAt.toInstant()).thenReturn(lastModifiedAtInstant);

            when(lastModifiedAtInstant.until(currentInstant, ChronoUnit.MINUTES)).thenReturn(new Random().nextLong(cacheLifespan + 1, 1000));

            remoteRates = OpenExchangeRatesCurrencyRatesApiResponse.builder()
                    .base(UUID.randomUUID().toString())
                    .rates(Map.of(UUID.randomUUID().toString(), new Random().nextDouble()))
                    .build();
            when(client.fetchCurrencyRates()).thenReturn(remoteRates);
        }

        @Test
        @DisplayName("it should fetch the rates from the server")
        void it_should_fetch_the_rates_from_the_server() {
            OpenExchangeRatesCurrencyRates rates = ratesManager.getRates();

            assertEquals(remoteRates.base(), rates.getBase());
            assertEquals(remoteRates.rates(), rates.getRates());
        }

        @Test
        @DisplayName("it should save the new rates in the cache")
        void it_should_save_the_new_rates_in_the_cache() throws IOException {
           ratesManager.getRates();

           verify(cacheManager).saveCache(OpenExchangeRatesCurrencyRatesCache.builder()
                           .base(remoteRates.base())
                           .rates(remoteRates.rates())
                           .lastUpdatedAt(currentDate)
                            .build());
        }

        @Test
        @DisplayName("it should not throw an exception if it fails to save the new rates in the cache")
        void it_should_not_throw_an_exception_if_it_fails_to_save_the_new_rates_in_the_cache() throws IOException {
            doThrow(IOException.class).when(cacheManager).saveCache(any());

            assertDoesNotThrow(() -> ratesManager.getRates());
        }
    }

    @Nested
    @DisplayName("When caching is disabled")
    class CacheDisabled {

        private OpenExchangeRatesCurrencyRatesApiResponse remoteRates;

        @BeforeEach
        void setUp() {
            when(cacheConfiguration.isEnabled()).thenReturn(false);

            remoteRates = OpenExchangeRatesCurrencyRatesApiResponse.builder()
                    .base(UUID.randomUUID().toString())
                    .rates(Map.of(UUID.randomUUID().toString(), new Random().nextDouble()))
                    .build();
            when(client.fetchCurrencyRates()).thenReturn(remoteRates);
        }

        @Test
        @DisplayName("it should fetch the rates from the server")
        void it_should_fetch_the_rates_from_the_server() {
            OpenExchangeRatesCurrencyRates rates = ratesManager.getRates();

            assertEquals(remoteRates.base(), rates.getBase());
            assertEquals(remoteRates.rates(), rates.getRates());
        }

        @Test
        @DisplayName("it not should save the new rates in the cache")
        void it_not_should_save_the_new_rates_in_the_cache() throws IOException {
            ratesManager.getRates();

            verify(cacheManager, never()).saveCache(any());
        }
    }

    @Nested
    @DisplayName("When no rates cache exists")
    class NoRatesCacheExists {

        private OpenExchangeRatesCurrencyRatesApiResponse remoteRates;

        @BeforeEach
        void setUp() {
            when(cacheManager.fetchCache()).thenReturn(Optional.empty());
            remoteRates = OpenExchangeRatesCurrencyRatesApiResponse.builder()
                    .base(UUID.randomUUID().toString())
                    .rates(Map.of(UUID.randomUUID().toString(), new Random().nextDouble()))
                    .build();
            when(client.fetchCurrencyRates()).thenReturn(remoteRates);
        }

        @Test
        @DisplayName("it should fetch the rates from the server")
        void it_should_fetch_the_rates_from_the_server() {
            OpenExchangeRatesCurrencyRates rates = ratesManager.getRates();

            assertEquals(remoteRates.base(), rates.getBase());
            assertEquals(remoteRates.rates(), rates.getRates());
        }

        @Test
        @DisplayName("it should save the new rates in the cache")
        void it_should_save_the_new_rates_in_the_cache() throws IOException {
            ratesManager.getRates();

            verify(cacheManager).saveCache(OpenExchangeRatesCurrencyRatesCache.builder()
                    .base(remoteRates.base())
                    .rates(remoteRates.rates())
                    .lastUpdatedAt(currentDate)
                    .build());
        }

        @Test
        @DisplayName("it should not throw an exception if it fails to save the new rates in the cache")
        void it_should_not_throw_an_exception_if_it_fails_to_save_the_new_rates_in_the_cache() throws IOException {
            doThrow(IOException.class).when(cacheManager).saveCache(any());

            assertDoesNotThrow(() -> ratesManager.getRates());
        }
    }

}