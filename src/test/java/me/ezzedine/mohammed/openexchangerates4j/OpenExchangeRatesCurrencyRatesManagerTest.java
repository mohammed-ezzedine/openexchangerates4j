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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class OpenExchangeRatesCurrencyRatesManagerTest {

    private OpenExchangeRatesCurrencyRatesManager ratesManager;
    private OpenExchangeRatesCurrencyRatesCacheManager cacheManager;
    private OpenExchangeRatesCurrencyRatesFailureTracker failureTracker;

    private Date currentDate;
    private Instant currentInstant;
    private OpenExchangeRatesClient client;
    private OpenExchangeRatesConfiguration.CacheConfiguration cacheConfiguration;
    private int cacheLifespan;
    private int retryBackoff;

    @BeforeEach
    void setUp() {
        client = mock(OpenExchangeRatesClient.class);

        OpenExchangeRatesCurrencyRatesCacheManagerFactory cacheManagerFactory = mock(OpenExchangeRatesCurrencyRatesCacheManagerFactory.class);
        cacheManager = mock(OpenExchangeRatesCurrencyRatesCacheManager.class);
        when(cacheManagerFactory.get()).thenReturn(cacheManager);

        OpenExchangeRatesCurrencyRatesFailureTrackerFactory failureTrackerFactory = mock(OpenExchangeRatesCurrencyRatesFailureTrackerFactory.class);
        failureTracker = mock(OpenExchangeRatesCurrencyRatesFailureTracker.class);
        when(failureTrackerFactory.get()).thenReturn(failureTracker);
        when(failureTracker.fetchFailure()).thenReturn(Optional.empty());

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

        OpenExchangeRatesConfiguration.CacheConfiguration.RetryConfiguration retryConfiguration = mock(OpenExchangeRatesConfiguration.CacheConfiguration.RetryConfiguration.class);
        retryBackoff = new Random().nextInt(1, 30);
        when(retryConfiguration.getBackoff()).thenReturn(retryBackoff);
        when(cacheConfiguration.getRetry()).thenReturn(retryConfiguration);

        when(configuration.getCache()).thenReturn(cacheConfiguration);

        ratesManager = new OpenExchangeRatesCurrencyRatesManager(client, cacheManagerFactory, failureTrackerFactory, openExchangeRatesDateFactory, configuration);
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
            assertEquals(cachedRates.getLastUpdatedAt(), rates.getLastUpdatedAt());
        }

        @Test
        @DisplayName("it should not mark the rates as stale")
        void it_should_not_mark_the_rates_as_stale() {
            OpenExchangeRatesCurrencyRates rates = ratesManager.getRates();

            assertFalse(rates.isStale());
        }

        @Test
        @DisplayName("it should not attempt to fetch the rates from the server")
        void it_should_not_attempt_to_fetch_the_rates_from_the_server() {
            ratesManager.getRates();

            verify(client, never()).fetchCurrencyRates();
        }
    }

    @Nested
    @DisplayName("When an inactive rates cache exists")
    class InactiveRatesCacheExists {
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

            when(lastModifiedAtInstant.until(currentInstant, ChronoUnit.MINUTES)).thenReturn(new Random().nextLong(cacheLifespan + 1, 1000));
        }

        @Nested
        @DisplayName("And the server fetch succeeds")
        class AndTheServerFetchSucceeds {
            private OpenExchangeRatesCurrencyRatesApiResponse remoteRates;

            @BeforeEach
            void setUp() {
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
                assertEquals(currentDate, rates.getLastUpdatedAt());
                assertFalse(rates.isStale());
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

            @Test
            @DisplayName("it should clear any previously recorded failed fetch attempt")
            void it_should_clear_any_previously_recorded_failed_fetch_attempt() throws IOException {
                ratesManager.getRates();

                verify(failureTracker).clearFailure();
            }

            @Test
            @DisplayName("it should not throw an exception if it fails to clear the previously recorded failed fetch attempt")
            void it_should_not_throw_an_exception_if_it_fails_to_clear_the_previously_recorded_failed_fetch_attempt() throws IOException {
                doThrow(IOException.class).when(failureTracker).clearFailure();

                assertDoesNotThrow(() -> ratesManager.getRates());
            }
        }

        @Nested
        @DisplayName("And the server fetch fails")
        class AndTheServerFetchFails {

            @BeforeEach
            void setUp() {
                when(client.fetchCurrencyRates()).thenThrow(new RuntimeException("server unavailable"));
            }

            @Test
            @DisplayName("it should return the stale rates from the cache")
            void it_should_return_the_stale_rates_from_the_cache() {
                OpenExchangeRatesCurrencyRates rates = ratesManager.getRates();

                assertEquals(cachedRates.getBase(), rates.getBase());
                assertEquals(cachedRates.getRates(), rates.getRates());
                assertEquals(cachedRates.getLastUpdatedAt(), rates.getLastUpdatedAt());
                assertTrue(rates.isStale());
            }

            @Test
            @DisplayName("it should record the failed fetch attempt")
            void it_should_record_the_failed_fetch_attempt() throws IOException {
                ratesManager.getRates();

                verify(failureTracker).recordFailure(OpenExchangeRatesCurrencyRatesFailure.builder()
                        .lastFailedAt(currentDate)
                        .build());
            }

            @Test
            @DisplayName("it should not throw an exception if it fails to record the failed fetch attempt")
            void it_should_not_throw_an_exception_if_it_fails_to_record_the_failed_fetch_attempt() throws IOException {
                doThrow(IOException.class).when(failureTracker).recordFailure(any());

                assertDoesNotThrow(() -> ratesManager.getRates());
            }
        }
    }

    @Nested
    @DisplayName("When caching is disabled")
    class CacheDisabled {

        @BeforeEach
        void setUp() {
            when(cacheConfiguration.isEnabled()).thenReturn(false);
        }

        @Nested
        @DisplayName("And the server fetch succeeds")
        class AndTheServerFetchSucceeds {
            private OpenExchangeRatesCurrencyRatesApiResponse remoteRates;

            @BeforeEach
            void setUp() {
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
        @DisplayName("And the server fetch fails")
        class AndTheServerFetchFails {

            @BeforeEach
            void setUp() {
                when(client.fetchCurrencyRates()).thenThrow(new RuntimeException("server unavailable"));
            }

            @Test
            @DisplayName("it should throw an unavailable exception since there is no cache to fall back on")
            void it_should_throw_an_unavailable_exception_since_there_is_no_cache_to_fall_back_on() {
                assertThrows(OpenExchangeRatesUnavailableException.class, () -> ratesManager.getRates());
            }

            @Test
            @DisplayName("it should record the failed fetch attempt")
            void it_should_record_the_failed_fetch_attempt() throws IOException {
                assertThrows(OpenExchangeRatesUnavailableException.class, () -> ratesManager.getRates());

                verify(failureTracker).recordFailure(any());
            }
        }
    }

    @Nested
    @DisplayName("When no rates cache exists")
    class NoRatesCacheExists {

        @BeforeEach
        void setUp() {
            when(cacheManager.fetchCache()).thenReturn(Optional.empty());
        }

        @Nested
        @DisplayName("And the server fetch succeeds")
        class AndTheServerFetchSucceeds {
            private OpenExchangeRatesCurrencyRatesApiResponse remoteRates;

            @BeforeEach
            void setUp() {
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
        @DisplayName("And the server fetch fails")
        class AndTheServerFetchFails {

            @BeforeEach
            void setUp() {
                when(client.fetchCurrencyRates()).thenThrow(new RuntimeException("server unavailable"));
            }

            @Test
            @DisplayName("it should throw an unavailable exception since there is no cache to fall back on")
            void it_should_throw_an_unavailable_exception_since_there_is_no_cache_to_fall_back_on() {
                assertThrows(OpenExchangeRatesUnavailableException.class, () -> ratesManager.getRates());
            }

            @Test
            @DisplayName("it should record the failed fetch attempt")
            void it_should_record_the_failed_fetch_attempt() throws IOException {
                assertThrows(OpenExchangeRatesUnavailableException.class, () -> ratesManager.getRates());

                verify(failureTracker).recordFailure(any());
            }
        }
    }

    @Nested
    @DisplayName("When a previous fetch attempt failed within the retry backoff window")
    class PreviousFailureWithinBackoffWindow {

        @BeforeEach
        void setUp() {
            Date lastFailedAt = mock(Date.class);
            Instant lastFailedAtInstant = mock(Instant.class);
            when(lastFailedAt.toInstant()).thenReturn(lastFailedAtInstant);
            when(lastFailedAtInstant.until(currentInstant, ChronoUnit.MINUTES)).thenReturn(new Random().nextLong(0, retryBackoff));

            when(failureTracker.fetchFailure()).thenReturn(Optional.of(
                    OpenExchangeRatesCurrencyRatesFailure.builder().lastFailedAt(lastFailedAt).build()
            ));
        }

        @Nested
        @DisplayName("And a cache exists")
        class AndACacheExists {
            private OpenExchangeRatesCurrencyRatesCache cachedRates;

            @BeforeEach
            void setUp() {
                Date lastUpdatedAt = mock(Date.class);
                Instant lastModifiedAtInstant = mock(Instant.class);
                when(lastUpdatedAt.toInstant()).thenReturn(lastModifiedAtInstant);
                when(lastModifiedAtInstant.until(currentInstant, ChronoUnit.MINUTES)).thenReturn(new Random().nextLong(cacheLifespan + 1, 1000));

                cachedRates = OpenExchangeRatesCurrencyRatesCache.builder()
                        .base(UUID.randomUUID().toString())
                        .rates(Map.of(UUID.randomUUID().toString(), new Random().nextDouble()))
                        .lastUpdatedAt(lastUpdatedAt)
                        .build();
                when(cacheManager.fetchCache()).thenReturn(Optional.of(cachedRates));
            }

            @Test
            @DisplayName("it should return the stale cached rates without contacting the server")
            void it_should_return_the_stale_cached_rates_without_contacting_the_server() {
                OpenExchangeRatesCurrencyRates rates = ratesManager.getRates();

                assertEquals(cachedRates.getBase(), rates.getBase());
                assertTrue(rates.isStale());
                verify(client, never()).fetchCurrencyRates();
            }
        }

        @Nested
        @DisplayName("And no cache exists")
        class AndNoCacheExists {

            @BeforeEach
            void setUp() {
                when(cacheManager.fetchCache()).thenReturn(Optional.empty());
            }

            @Test
            @DisplayName("it should throw an unavailable exception without contacting the server")
            void it_should_throw_an_unavailable_exception_without_contacting_the_server() {
                assertThrows(OpenExchangeRatesUnavailableException.class, () -> ratesManager.getRates());
                verify(client, never()).fetchCurrencyRates();
            }
        }
    }

    @Nested
    @DisplayName("When a previous fetch attempt failed outside the retry backoff window")
    class PreviousFailureOutsideBackoffWindow {

        @BeforeEach
        void setUp() {
            Date lastFailedAt = mock(Date.class);
            Instant lastFailedAtInstant = mock(Instant.class);
            when(lastFailedAt.toInstant()).thenReturn(lastFailedAtInstant);
            when(lastFailedAtInstant.until(currentInstant, ChronoUnit.MINUTES)).thenReturn(new Random().nextLong(retryBackoff, retryBackoff + 1000));

            when(failureTracker.fetchFailure()).thenReturn(Optional.of(
                    OpenExchangeRatesCurrencyRatesFailure.builder().lastFailedAt(lastFailedAt).build()
            ));

            when(cacheManager.fetchCache()).thenReturn(Optional.empty());
        }

        @Test
        @DisplayName("it should attempt to fetch the rates from the server again")
        void it_should_attempt_to_fetch_the_rates_from_the_server_again() {
            when(client.fetchCurrencyRates()).thenReturn(OpenExchangeRatesCurrencyRatesApiResponse.builder()
                    .base(UUID.randomUUID().toString())
                    .rates(Map.of(UUID.randomUUID().toString(), new Random().nextDouble()))
                    .build());

            ratesManager.getRates();

            verify(client).fetchCurrencyRates();
        }
    }

}
