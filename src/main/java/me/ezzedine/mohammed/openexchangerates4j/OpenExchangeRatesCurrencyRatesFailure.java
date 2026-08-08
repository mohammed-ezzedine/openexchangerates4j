package me.ezzedine.mohammed.openexchangerates4j;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
final class OpenExchangeRatesCurrencyRatesFailure {
    private Date lastFailedAt;
}
