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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenExchangeRatesCurrencyRatesFailureTrackerTest {

    private OpenExchangeRatesCurrencyRatesFailureTracker failureTracker;
    private Path rootPath;

    @BeforeEach
    void setUp() {
        FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        rootPath = fileSystem.getPath("");

        failureTracker = new OpenExchangeRatesCurrencyRatesFailureTracker(rootPath);
    }

    @Nested
    @DisplayName("When a failure record exists in the file system")
    class FailureRecordExists {

        private OpenExchangeRatesCurrencyRatesFailure failure;
        private Path filePath;

        @BeforeEach
        void setUp() throws IOException {
            filePath = buildFailureFilePath();
            Files.createFile(filePath);

            failure = failureRecord();
            Files.write(filePath, new ObjectMapper().writeValueAsBytes(failure));
        }

        @Test
        @DisplayName("it should return a non empty result when fetching the failure")
        void it_should_return_a_non_empty_result_when_fetching_the_failure() {
            assertTrue(failureTracker.fetchFailure().isPresent());
        }

        @Test
        @DisplayName("it should read the failure content properly from the file system")
        void it_should_read_the_failure_content_properly_from_the_file_system() {
            OpenExchangeRatesCurrencyRatesFailure fetchedFailure = failureTracker.fetchFailure().orElseThrow();
            assertEquals(failure, fetchedFailure);
        }

        @Test
        @DisplayName("it should return an empty result when an error occurs while reading the file")
        void it_should_return_an_empty_result_when_an_error_occurs_while_reading_the_file() throws IOException {
            Files.write(filePath, new ObjectMapper().writeValueAsBytes(UUID.randomUUID().toString()));
            assertTrue(failureTracker.fetchFailure().isEmpty());
        }

        @Test
        @DisplayName("it should override the failure record in the file system when calling record failure")
        void it_should_override_the_failure_record_in_the_file_system_when_calling_record_failure() throws IOException {
            OpenExchangeRatesCurrencyRatesFailure updatedFailure = failureRecord();
            failureTracker.recordFailure(updatedFailure);

            Path failureFilePath = buildFailureFilePath();
            byte[] bytes = Files.readAllBytes(failureFilePath);
            OpenExchangeRatesCurrencyRatesFailure fetchedFailure = new ObjectMapper().readValue(bytes, OpenExchangeRatesCurrencyRatesFailure.class);

            assertEquals(updatedFailure, fetchedFailure);
        }

        @Test
        @DisplayName("it should delete the failure record from the file system when calling clear failure")
        void it_should_delete_the_failure_record_from_the_file_system_when_calling_clear_failure() throws IOException {
            failureTracker.clearFailure();

            assertFalse(Files.exists(buildFailureFilePath()));
        }
    }

    @Nested
    @DisplayName("When no failure record exists in the file system")
    class FailureRecordDoesNotExist {

        @Test
        @DisplayName("it should return an empty optional when fetching the failure")
        void it_should_return_an_empty_optional_when_fetching_the_failure() {
            assertTrue(failureTracker.fetchFailure().isEmpty());
        }

        @Test
        @DisplayName("it should save the failure record in the file system when calling record failure")
        void it_should_save_the_failure_record_in_the_file_system_when_calling_record_failure() throws IOException {
            OpenExchangeRatesCurrencyRatesFailure failure = failureRecord();
            failureTracker.recordFailure(failure);

            Path failureFilePath = buildFailureFilePath();
            byte[] bytes = Files.readAllBytes(failureFilePath);
            OpenExchangeRatesCurrencyRatesFailure fetchedFailure = new ObjectMapper().readValue(bytes, OpenExchangeRatesCurrencyRatesFailure.class);

            assertEquals(failure, fetchedFailure);
        }

        @Test
        @DisplayName("it should not throw an exception when calling clear failure")
        void it_should_not_throw_an_exception_when_calling_clear_failure() {
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> failureTracker.clearFailure());
        }
    }

    private Path buildFailureFilePath() {
        return rootPath.resolve("openexchangerates_failure.json");
    }

    private static OpenExchangeRatesCurrencyRatesFailure failureRecord() {
        return OpenExchangeRatesCurrencyRatesFailure.builder()
                .lastFailedAt(new Date())
                .build();
    }
}
