package me.ezzedine.mohammed.openexchangerates4j;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

import java.util.Map;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
record OpenExchangeRatesCurrencyRatesApiResponse(
    String base,
    Map<String, Double> rates
) { }
