package me.ezzedine.mohammed.openexchangerates4j;

public class OpenExchangeRatesUnavailableException extends RuntimeException {

    private static final String MESSAGE =
            "Unable to retrieve currency exchange rates. The Open Exchange Rates server could not be reached "
                    + "and there is no previously cached data available to fall back on.";

    public OpenExchangeRatesUnavailableException() {
        super(MESSAGE);
    }

    public OpenExchangeRatesUnavailableException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
