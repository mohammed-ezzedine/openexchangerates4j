package me.ezzedine.mohammed.openexchangerates4j;

import lombok.RequiredArgsConstructor;

import java.nio.file.Path;

@RequiredArgsConstructor
class OpenExchangeRatesCurrencyRatesCacheManagerFactory {

    public OpenExchangeRatesCurrencyRatesCacheManager get() {
        return new OpenExchangeRatesCurrencyRatesCacheManager(Path.of(""));
    }
}
