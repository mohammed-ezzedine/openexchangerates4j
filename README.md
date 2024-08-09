# Openexchangerates4j

A java client for [open exchange rates](https://openexchangerates.org/) that abstracts the logic to convert
money between currencies from your application.

# Features
1. It supports converting between all the supported currencies available at [open exchange rates](https://openexchangerates.org/).
2. It supports caching the rates results to avoid exceeding the monthly limit set by your subscription. (Great for free tier subscriptions)
3. It supports configuring the lifespan of the cache in minutes.

# Requirements
The library is only compatible with spring boot applications.

# How to Use
1. Import the library into your application:

   For Maven:

        <dependency>
            <groupId>me.ezzedine.mohammed</groupId>
            <artifactId>openexchangerates4j</artifactId>
            <version>1.0.0</version>
        </dependency>

   For gradle:

        implementation("me.ezzedine.mohammed:openexchangerates4j:1.0.0")
2. Add the following configuration into your properties file (typically `application.yml` or `application.properties`):

        openexchangerates:
            appId: ${YOU_OPENEXCHANGERATES_APP_ID}
            cache:
                enabled: true
                lifespan: 120 // validity duration of the cache in minutes

3. Inject the conversion service and start using it:

         @Autowired
         private OpenExchangeRatesCurrencyExchangeService exchangeService

         BigDecimal convertedAmount = exchangeService.convert(BigDecimal.valueOf(100), 
                                                              Currency.getInstance("USD"), 
                                                              Currency.getInstance("EUR"));