package me.ezzedine.mohammed.openexchangerates4j;

import java.nio.file.Path;

class OpenExchangeRatesCurrencyRatesFailureTrackerFactory {

    public OpenExchangeRatesCurrencyRatesFailureTracker get() {
        return new OpenExchangeRatesCurrencyRatesFailureTracker(Path.of(""));
    }
}
