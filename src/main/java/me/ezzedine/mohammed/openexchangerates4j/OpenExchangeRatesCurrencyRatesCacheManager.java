package me.ezzedine.mohammed.openexchangerates4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@RequiredArgsConstructor
class OpenExchangeRatesCurrencyRatesCacheManager {

    private static final String CACHE_FILE_NAME = "openexchangerates_cache.json";

    private final Path rootPath;

    public Optional<OpenExchangeRatesCurrencyRatesCache> fetchCache() {
        try {
            Path cacheFile = rootPath.resolve(CACHE_FILE_NAME);
            boolean cacheExists = Files.exists(cacheFile);
            if (cacheExists) {

                byte[] bytes = Files.readAllBytes(cacheFile);
                OpenExchangeRatesCurrencyRatesCache cache = new ObjectMapper().readValue(bytes, OpenExchangeRatesCurrencyRatesCache.class);
                return Optional.ofNullable(cache);
            }

            return Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public void saveCache(OpenExchangeRatesCurrencyRatesCache cache) throws IOException {
        Path cacheFile = rootPath.resolve(CACHE_FILE_NAME);
        Files.deleteIfExists(cacheFile);
        Files.createFile(cacheFile);

        Files.write(cacheFile, new ObjectMapper().writeValueAsBytes(cache));
    }
}
