package me.ezzedine.mohammed.openexchangerates4j;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
final class OpenExchangeRatesCurrencyRatesCache {
    private String base;
    private Map<String, Double> rates;
    private Date lastUpdatedAt;
}
