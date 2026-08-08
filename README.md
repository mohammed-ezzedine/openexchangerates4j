# Openexchangerates4j

A java client for [open exchange rates](https://openexchangerates.org/) that abstracts the logic to convert
money between currencies from your application.

# Features
1. It supports converting between all the supported currencies available at [open exchange rates](https://openexchangerates.org/).
2. It supports caching the rates results to avoid exceeding the monthly limit set by your subscription. (Great for free tier subscriptions)
3. It supports configuring the lifespan of the cache in minutes.
4. It supports optionally, proactively refreshing the cache in the background before it expires, so requests rarely have to wait on a live fetch.
5. If the server is unreachable when the cache is stale, it falls back to serving the last cached rates and clearly flags the conversion result as stale.
6. It throttles retries after a failed fetch attempt (configurable backoff) so it doesn't hammer the server on every request while it's down.

> **Note:** version `2.0.0` changes the return type of `OpenExchangeRatesCurrencyExchangeService#convert`. See [Migrating from 1.x](#migrating-from-1x) below.

# Requirements
The library is only compatible with spring boot applications.

# How to Use
1. Import the library into your application:

   For Maven:

        <dependency>
            <groupId>me.ezzedine.mohammed</groupId>
            <artifactId>openexchangerates4j</artifactId>
            <version>2.0.0</version>
        </dependency>

   For gradle:

        implementation("me.ezzedine.mohammed:openexchangerates4j:2.0.0")
2. Add the following configuration into your properties file (typically `application.yml` or `application.properties`):

        openexchangerates:
            appId: ${YOU_OPENEXCHANGERATES_APP_ID}
            cache:
                enabled: true
                lifespan: 120 // validity duration of the cache in minutes
                refresh:
                    enabled: false // whether to proactively refresh the cache in the background, using lifespan as the interval
                retry:
                    backoff: 5 // minutes to wait after a failed fetch attempt before retrying the server

3. Inject the conversion service and start using it:

         @Autowired
         private OpenExchangeRatesCurrencyExchangeService exchangeService

         OpenExchangeRatesConversionResult result = exchangeService.convert(BigDecimal.valueOf(100),
                                                              Currency.getInstance("USD"),
                                                              Currency.getInstance("EUR"));

         BigDecimal convertedAmount = result.getAmount();
         if (result.isStale()) {
             // the Open Exchange Rates server was unreachable; convertedAmount was computed
             // from the last cached rates and may not reflect the current real-world rate
             log.warn(result.getWarning());
         }

# Error Handling

If the server is unreachable **and** there is no previously cached data to fall back on, `convert` throws an
unchecked `OpenExchangeRatesUnavailableException` explaining that no rates could be retrieved. If cached data
does exist, `convert` never throws for a server outage — instead it returns a `OpenExchangeRatesConversionResult`
with `stale` set to `true` and a `warning` describing the situation.

# Caching, Refresh & Reliability

The full lifecycle of a rates lookup:
1. If a cached result exists and is younger than `cache.lifespan` minutes, it's used as-is.
2. Otherwise, a live fetch is attempted. On success, the cache is updated and used.
3. If the live fetch fails and a (possibly expired) cache exists, that cached data is served, but the
   result is marked `stale` with a `warning` explaining the server was unreachable.
4. If the live fetch fails and there's no cache at all, `OpenExchangeRatesUnavailableException` is thrown.
5. After any failed fetch, the failure is recorded locally; further attempts are skipped (falling straight
   to steps 3/4) until `cache.retry.backoff` minutes have passed, to avoid hammering a down server.
6. If `cache.refresh.enabled` is `true`, the library also proactively repeats this lookup in the background
   every `cache.lifespan` minutes, so the cache is usually warm by the time a real request comes in.

# Migrating from 1.x

`OpenExchangeRatesCurrencyExchangeService#convert` now returns `OpenExchangeRatesConversionResult` instead of
`BigDecimal`. Update call sites to read `.getAmount()` instead of using the return value directly, and
optionally check `.isStale()` / `.getWarning()` to detect degraded (server-down) responses.