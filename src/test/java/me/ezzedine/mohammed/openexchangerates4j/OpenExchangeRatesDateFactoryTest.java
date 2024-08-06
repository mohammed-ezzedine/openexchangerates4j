package me.ezzedine.mohammed.openexchangerates4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenExchangeRatesDateFactoryTest {

    @Test
    @DisplayName("generates a non null date")
    void generates_a_non_null_date() {
        assertNotNull(new OpenExchangeRatesDateFactory().now());
    }
}