package me.ezzedine.mohammed.openexchangerates4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenExchangeRatesCurrencyExchangeServiceTest {

    private static final Currency USD_CURRENCY = Currency.getInstance("USD");
    private static final Currency EUR_CURRENCY = Currency.getInstance("EUR");
    private static final Currency CHF_CURRENCY = Currency.getInstance("CHF");
    public static final double EUR_RATE = new Random().nextDouble(0, 2);
    public static final double CHF_RATE = new Random().nextDouble(0, 2);
    private OpenExchangeRatesCurrencyExchangeService currencyExchangeManager;

    @BeforeEach
    void setUp() {
        OpenExchangeRatesCurrencyRatesManager ratesManager = mock(OpenExchangeRatesCurrencyRatesManager.class);
        when(ratesManager.getRates()).thenReturn(OpenExchangeRatesCurrencyRates.builder()
                .base("USD")
                .rates(Map.of(
                        "CHF", CHF_RATE,
                        "EUR", EUR_RATE
                ))
                .build());

        currencyExchangeManager = new OpenExchangeRatesCurrencyExchangeService(ratesManager);
    }

    @Test
    @DisplayName("it should fail if the specified amount is negative")
    void it_should_fail_if_the_specified_amount_is_negative() {
        assertThrows(IllegalArgumentException.class, () -> currencyExchangeManager.convert(BigDecimal.valueOf(-1), USD_CURRENCY, EUR_CURRENCY));
    }

    @Test
    @DisplayName("it should return zero if the specified amount is zero")
    void it_should_return_zero_if_the_specified_amount_is_zero() {
        BigDecimal result = currencyExchangeManager.convert(BigDecimal.ZERO, EUR_CURRENCY, CHF_CURRENCY);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("it should return the same amount when the source and target currencies are the same")
    void it_should_return_the_same_amount_when_the_source_and_target_currencies_are_the_same() {
        BigDecimal amount = BigDecimal.valueOf(new Random().nextDouble(10, 100));
        BigDecimal result = currencyExchangeManager.convert(amount, EUR_CURRENCY, EUR_CURRENCY);
        assertEquals(amount, result);
    }


    @Test
    @DisplayName("it should convert the amount correctly when the source currency is usd")
    void it_should_convert_the_amount_correctly_when_the_source_currency_is_usd() {
        BigDecimal amount = BigDecimal.valueOf(new Random().nextDouble(10, 100));
        BigDecimal result = currencyExchangeManager.convert(amount, USD_CURRENCY, EUR_CURRENCY);
       assertEquals(amount.multiply(BigDecimal.valueOf(EUR_RATE)), result);
    }

    @Test
    @DisplayName("it should convert the amount correctly when the target currency is usd")
    void it_should_convert_the_amount_correctly_when_the_target_currency_is_usd() {
        BigDecimal amount = BigDecimal.valueOf(new Random().nextDouble(10, 100));
        BigDecimal result = currencyExchangeManager.convert(amount, CHF_CURRENCY, USD_CURRENCY);
        assertEquals(amount.divide(BigDecimal.valueOf(CHF_RATE), RoundingMode.DOWN), result);
    }

    @Test
    @DisplayName("it should convert the amount correctly when neither the source nor the target currency is usd")
    void it_should_convert_the_amount_correctly_when_neither_the_source_nor_the_target_currency_is_usd() {
        BigDecimal amount = BigDecimal.valueOf(new Random().nextDouble(10, 100));
        BigDecimal result = currencyExchangeManager.convert(amount, CHF_CURRENCY, EUR_CURRENCY);
        BigDecimal expected = amount
                .divide(BigDecimal.valueOf(CHF_RATE), RoundingMode.DOWN)
                .multiply(BigDecimal.valueOf(EUR_RATE));
        assertEquals(expected, result);
    }
}