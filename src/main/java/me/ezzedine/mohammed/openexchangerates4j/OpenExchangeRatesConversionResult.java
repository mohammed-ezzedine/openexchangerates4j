package me.ezzedine.mohammed.openexchangerates4j;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class OpenExchangeRatesConversionResult {
    BigDecimal amount;
    boolean stale;
    String warning;

    private OpenExchangeRatesConversionResult(BigDecimal amount, boolean stale, String warning) {
        this.amount = amount;
        this.stale = stale;
        this.warning = warning;
    }

    public static OpenExchangeRatesConversionResult fresh(BigDecimal amount) {
        return new OpenExchangeRatesConversionResult(amount, false, null);
    }

    public static OpenExchangeRatesConversionResult stale(BigDecimal amount, String warning) {
        return new OpenExchangeRatesConversionResult(amount, true, warning);
    }
}
