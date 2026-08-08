package me.ezzedine.mohammed.openexchangerates4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenExchangeRatesConversionResultTest {

    @Test
    @DisplayName("fresh should build a non-stale result with no warning")
    void fresh_should_build_a_non_stale_result_with_no_warning() {
        BigDecimal amount = BigDecimal.valueOf(new Random().nextDouble(1, 100));

        OpenExchangeRatesConversionResult result = OpenExchangeRatesConversionResult.fresh(amount);

        assertEquals(amount, result.getAmount());
        assertFalse(result.isStale());
        assertNull(result.getWarning());
    }

    @Test
    @DisplayName("stale should build a stale result carrying the given warning")
    void stale_should_build_a_stale_result_carrying_the_given_warning() {
        BigDecimal amount = BigDecimal.valueOf(new Random().nextDouble(1, 100));
        String warning = "server unavailable";

        OpenExchangeRatesConversionResult result = OpenExchangeRatesConversionResult.stale(amount, warning);

        assertEquals(amount, result.getAmount());
        assertTrue(result.isStale());
        assertEquals(warning, result.getWarning());
    }
}
