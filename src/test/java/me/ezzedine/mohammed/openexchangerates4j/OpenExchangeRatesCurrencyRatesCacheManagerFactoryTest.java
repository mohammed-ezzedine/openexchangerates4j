package me.ezzedine.mohammed.openexchangerates4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenExchangeRatesCurrencyRatesCacheManagerFactoryTest {

    @Test
    @DisplayName("returns a non nullable instance")
    void returns_a_non_nullable_instance() {
        assertNotNull(new OpenExchangeRatesCurrencyRatesCacheManagerFactory().get());
    }

}