package me.ezzedine.mohammed.openexchangerates4j;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

@RequiredArgsConstructor
public class OpenExchangeRatesCurrencyExchangeService {

    private final OpenExchangeRatesCurrencyRatesManager ratesManager;

    public BigDecimal convert(BigDecimal amount, Currency sourceCurrency, Currency targetCurrency) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Value must be greater than zero");
        }

        if (amount.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }

        if (sourceCurrency == targetCurrency) {
            return amount;
        }

        OpenExchangeRatesCurrencyRates rates = ratesManager.getRates();
        if (rates.getBase().equals(sourceCurrency.getCurrencyCode())) {
            return convertFromBaseCurrency(amount, targetCurrency, rates);
        }

        if (rates.getBase().equals(targetCurrency.getCurrencyCode())) {
            return convertToBaseCurrency(amount, sourceCurrency, rates);
        }

        BigDecimal amountInBaseCurrency = convertToBaseCurrency(amount, sourceCurrency, rates);
        return convertFromBaseCurrency(amountInBaseCurrency, targetCurrency, rates);
    }

    private static BigDecimal convertFromBaseCurrency(BigDecimal amount, Currency targetCurrency, OpenExchangeRatesCurrencyRates rates) {
        return amount.multiply(BigDecimal.valueOf(rates.getRates().get(targetCurrency.getCurrencyCode())));
    }

    private static BigDecimal convertToBaseCurrency(BigDecimal amount, Currency sourceCurrency, OpenExchangeRatesCurrencyRates rates) {
        return amount.divide(BigDecimal.valueOf(rates.getRates().get(sourceCurrency.getCurrencyCode())), RoundingMode.DOWN);
    }
}
