package me.ezzedine.mohammed.openexchangerates4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@RequiredArgsConstructor
class OpenExchangeRatesCurrencyRatesFailureTracker {

    private static final String FAILURE_FILE_NAME = "openexchangerates_failure.json";

    private final Path rootPath;

    public Optional<OpenExchangeRatesCurrencyRatesFailure> fetchFailure() {
        try {
            Path failureFile = rootPath.resolve(FAILURE_FILE_NAME);
            boolean failureExists = Files.exists(failureFile);
            if (failureExists) {
                byte[] bytes = Files.readAllBytes(failureFile);
                OpenExchangeRatesCurrencyRatesFailure failure = new ObjectMapper().readValue(bytes, OpenExchangeRatesCurrencyRatesFailure.class);
                return Optional.ofNullable(failure);
            }

            return Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public void recordFailure(OpenExchangeRatesCurrencyRatesFailure failure) throws IOException {
        Path failureFile = rootPath.resolve(FAILURE_FILE_NAME);
        Files.deleteIfExists(failureFile);
        Files.createFile(failureFile);

        Files.write(failureFile, new ObjectMapper().writeValueAsBytes(failure));
    }

    public void clearFailure() throws IOException {
        Files.deleteIfExists(rootPath.resolve(FAILURE_FILE_NAME));
    }
}
