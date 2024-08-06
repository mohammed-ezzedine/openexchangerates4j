package me.ezzedine.mohammed.openexchangerates4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenExchangeRatesApiBaseUrlProviderTest {

    @Test
    @DisplayName("it should return the correct base url")
    void it_should_return_the_correct_base_url() {
        assertEquals("https://openexchangerates.org/api", new OpenExchangeRatesApiBaseUrlProvider().get());
    }

}