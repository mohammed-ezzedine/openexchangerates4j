package me.ezzedine.mohammed.openexchangerates4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class OpenExchangeRatesClientIntegrationTest {

    public static final String APP_ID = "app-id";
    private OpenExchangeRatesClient client;
    private ClientAndServer clientAndServer;

    @BeforeEach
    void setUp() {
        clientAndServer = startClientAndServer();

        OpenExchangeRatesConfiguration configuration = new OpenExchangeRatesConfiguration();
        configuration.setAppId(APP_ID);
        OpenExchangeRatesApiBaseUrlProvider apiBaseUrlProvider = mock(OpenExchangeRatesApiBaseUrlProvider.class);
        when(apiBaseUrlProvider.get()).thenReturn("http://localhost:%s".formatted(clientAndServer.getPort()));
        client = new OpenExchangeRatesClient(configuration, apiBaseUrlProvider);
    }

    @AfterEach
    void tearDown() {
        clientAndServer.stop();
    }

    @Test
    @DisplayName("it should return the rates from the server correctly")
    void it_should_return_the_rates_from_the_server_correctly() {
        clientAndServer
                .when(request()
                        .withMethod("GET")
                        .withPath("/latest.json")
                        .withQueryStringParameter("app_id", APP_ID))
                .respond(response()
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("""
                            {
                              "disclaimer": "Usage subject to terms: https://openexchangerates.org/terms",
                              "license": "https://openexchangerates.org/license",
                              "timestamp": 1722970800,
                              "base": "USD",
                              "rates": {
                                "CHF": 0.890161,
                                "EUR": 0.934739
                              }
                            }
                            """
                        ));

        OpenExchangeRatesCurrencyRatesApiResponse response = client.fetchCurrencyRates();

        assertEquals("USD", response.base());
        assertEquals(0.890161, response.rates().get("CHF"));
        assertEquals(0.934739, response.rates().get("EUR"));
    }
}