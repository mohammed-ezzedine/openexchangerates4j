package me.ezzedine.mohammed.openexchangerates4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenExchangeRatesUnavailableExceptionTest {

    @Test
    @DisplayName("it should have a non-blank message and no cause when built without a cause")
    void it_should_have_a_non_blank_message_and_no_cause_when_built_without_a_cause() {
        OpenExchangeRatesUnavailableException exception = new OpenExchangeRatesUnavailableException();

        assertFalse(exception.getMessage().isBlank());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("it should carry the given cause when built with one")
    void it_should_carry_the_given_cause_when_built_with_one() {
        RuntimeException cause = new RuntimeException("network error");
        OpenExchangeRatesUnavailableException exception = new OpenExchangeRatesUnavailableException(cause);

        assertFalse(exception.getMessage().isBlank());
        assertSame(cause, exception.getCause());
    }

    @Test
    @DisplayName("it should use the same message regardless of the constructor used")
    void it_should_use_the_same_message_regardless_of_the_constructor_used() {
        assertTrue(new OpenExchangeRatesUnavailableException().getMessage()
                .equals(new OpenExchangeRatesUnavailableException(new RuntimeException()).getMessage()));
    }
}
