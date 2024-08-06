package me.ezzedine.mohammed.openexchangerates4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenExchangeRatesCurrencyRatesCacheManagerTest {

    private OpenExchangeRatesCurrencyRatesCacheManager cacheManager;
    private Path rootPath;

    @BeforeEach
    void setUp() {
        FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        rootPath = fileSystem.getPath("");

        cacheManager = new OpenExchangeRatesCurrencyRatesCacheManager(rootPath);
    }

    @Nested
    @DisplayName("When the cache exists in the file system")
    class CacheExists {

        private OpenExchangeRatesCurrencyRatesCache cache;
        private Path filePath;

        @BeforeEach
        void setUp() throws IOException {
            filePath = buildCacheFilePath();
            Files.createFile(filePath);

            cache = ratesCache();
            Files.write(filePath, new ObjectMapper().writeValueAsBytes(cache));
        }

        @Test
        @DisplayName("it should return a non empty result when fetching the cache")
        void it_should_return_a_non_empty_result_when_fetching_the_cache() {
            assertTrue(cacheManager.fetchCache().isPresent());
        }

        @Test
        @DisplayName("it should read the cache content properly from the file system")
        void it_should_read_the_cache_content_properly_from_the_file_system() {
            OpenExchangeRatesCurrencyRatesCache fetchedCache = cacheManager.fetchCache().orElseThrow();
            assertEquals(cache, fetchedCache);
        }

        @Test
        @DisplayName("it should return an empty result when an error occurs while reading the file")
        void it_should_return_an_empty_result_when_an_error_occurs_while_reading_the_file() throws IOException {
            Files.write(filePath, new ObjectMapper().writeValueAsBytes(UUID.randomUUID().toString()));
            assertTrue(cacheManager.fetchCache().isEmpty());
        }

        @Test
        @DisplayName("it should override the cache in the file system when calling save cache")
        void it_should_override_the_cache_in_the_file_system_when_calling_save_cache() throws IOException {
            OpenExchangeRatesCurrencyRatesCache updatedCache = ratesCache();
            cacheManager.saveCache(updatedCache);

            Path cacheFilePath = buildCacheFilePath();
            byte[] bytes = Files.readAllBytes(cacheFilePath);
            OpenExchangeRatesCurrencyRatesCache fetchedCache = new ObjectMapper().readValue(bytes, OpenExchangeRatesCurrencyRatesCache.class);

            assertEquals(updatedCache, fetchedCache);
        }
    }

    @Nested
    @DisplayName("When the cache does not exist in the file system")
    class CacheDoesNotExist {

        @Test
        @DisplayName("it should return an empty optional when fetching the cache")
        void it_should_return_an_empty_optional_when_fetching_the_cache() {
            assertTrue(cacheManager.fetchCache().isEmpty());
        }

        @Test
        @DisplayName("it should save the cache in the file system when calling save cache")
        void it_should_save_the_cache_in_the_file_system_when_calling_save_cache() throws IOException {
            OpenExchangeRatesCurrencyRatesCache cache = ratesCache();
            cacheManager.saveCache(cache);

            Path cacheFilePath = buildCacheFilePath();
            byte[] bytes = Files.readAllBytes(cacheFilePath);
            OpenExchangeRatesCurrencyRatesCache fetchedCache = new ObjectMapper().readValue(bytes, OpenExchangeRatesCurrencyRatesCache.class);

            assertEquals(cache, fetchedCache);
        }
    }

    private Path buildCacheFilePath() {
        return rootPath.resolve("openexchangerates_cache.json");
    }

    private static OpenExchangeRatesCurrencyRatesCache ratesCache() {
        return OpenExchangeRatesCurrencyRatesCache.builder()
                .base(UUID.randomUUID().toString())
                .rates(Map.of(
                        UUID.randomUUID().toString(), new Random().nextDouble()
                ))
                .lastUpdatedAt(new Date())
                .build();
    }
}