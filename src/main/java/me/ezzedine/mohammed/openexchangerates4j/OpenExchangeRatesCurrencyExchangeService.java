package me.ezzedine.mohammed.openexchangerates4j;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

@RequiredArgsConstructor
public class OpenExchangeRatesCurrencyExchangeService {

    private final OpenExchangeRatesCurrencyRatesManager ratesManager;

    public OpenExchangeRatesConversionResult convert(BigDecimal amount, Currency sourceCurrency, Currency targetCurrency) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Value must be greater than zero");
        }

        if (amount.equals(BigDecimal.ZERO)) {
            return OpenExchangeRatesConversionResult.fresh(BigDecimal.ZERO);
        }

        if (sourceCurrency == targetCurrency) {
            return OpenExchangeRatesConversionResult.fresh(amount);
        }

        OpenExchangeRatesCurrencyRates rates = ratesManager.getRates();
        BigDecimal convertedAmount;
        if (rates.getBase().equals(sourceCurrency.getCurrencyCode())) {
            convertedAmount = convertFromBaseCurrency(amount, targetCurrency, rates);
        } else if (rates.getBase().equals(targetCurrency.getCurrencyCode())) {
            convertedAmount = convertToBaseCurrency(amount, sourceCurrency, rates);
        } else {
            BigDecimal amountInBaseCurrency = convertToBaseCurrency(amount, sourceCurrency, rates);
            convertedAmount = convertFromBaseCurrency(amountInBaseCurrency, targetCurrency, rates);
        }

        return rates.isStale()
                ? OpenExchangeRatesConversionResult.stale(convertedAmount, buildStaleWarning(rates))
                : OpenExchangeRatesConversionResult.fresh(convertedAmount);
    }

    private static String buildStaleWarning(OpenExchangeRatesCurrencyRates rates) {
        return "The Open Exchange Rates server was unreachable; this result uses cached exchange rates last updated at "
                + rates.getLastUpdatedAt() + ". The data may be out of date.";
    }

    private static BigDecimal convertFromBaseCurrency(BigDecimal amount, Currency targetCurrency, OpenExchangeRatesCurrencyRates rates) {
        return amount.multiply(BigDecimal.valueOf(rates.getRates().get(targetCurrency.getCurrencyCode())));
    }

    private static BigDecimal convertToBaseCurrency(BigDecimal amount, Currency sourceCurrency, OpenExchangeRatesCurrencyRates rates) {
        return amount.divide(BigDecimal.valueOf(rates.getRates().get(sourceCurrency.getCurrencyCode())), RoundingMode.DOWN);
    }
}
