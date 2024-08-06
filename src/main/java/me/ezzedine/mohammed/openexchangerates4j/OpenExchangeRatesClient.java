package me.ezzedine.mohammed.openexchangerates4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RequiredArgsConstructor
class OpenExchangeRatesClient {

    private final OpenExchangeRatesConfiguration configuration;
    private final OpenExchangeRatesApiBaseUrlProvider urlProvider;

    public OpenExchangeRatesCurrencyRatesApiResponse fetchCurrencyRates() {
        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(getUrl()))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                return new ObjectMapper().readValue(response.body(), OpenExchangeRatesCurrencyRatesApiResponse.class);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String getUrl() {
        return "%s/latest.json?app_id=%s".formatted(urlProvider.get(), configuration.getAppId());
    }
}
