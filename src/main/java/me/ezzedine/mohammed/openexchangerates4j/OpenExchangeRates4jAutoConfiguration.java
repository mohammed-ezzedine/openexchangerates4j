package me.ezzedine.mohammed.openexchangerates4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(OpenExchangeRatesConfiguration.class)
public class OpenExchangeRates4jAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenExchangeRatesCurrencyExchangeService openExchangeRatesCurrencyExchangeService(OpenExchangeRatesCurrencyRatesManager ratesManager) {
        log.info("Registering bean OpenExchangeRatesCurrencyExchangeService");
        return new OpenExchangeRatesCurrencyExchangeService(ratesManager);
    }

    @Bean
    @ConditionalOnMissingBean
    OpenExchangeRatesClient openExchangeRatesClient(OpenExchangeRatesConfiguration configuration) {
        log.info("Registering bean OpenExchangeRatesClient");
        return new OpenExchangeRatesClient(configuration, new OpenExchangeRatesApiBaseUrlProvider());
    }

    @Bean
    @ConditionalOnMissingBean
    OpenExchangeRatesCurrencyRatesCacheManagerFactory openExchangeRatesCurrencyRatesCacheManagerFactory() {
        log.info("Registering bean OpenExchangeRatesCurrencyRatesCacheManagerFactory");
        return new OpenExchangeRatesCurrencyRatesCacheManagerFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    OpenExchangeRatesDateFactory openExchangeRatesDateFactory() {
        log.info("Registering bean OpenExchangeRatesDateFactory");
        return new OpenExchangeRatesDateFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    OpenExchangeRatesCurrencyRatesManager openExchangeCurrencyRatesManager(OpenExchangeRatesConfiguration configuration,
                                                                           OpenExchangeRatesClient client,
                                                                           OpenExchangeRatesCurrencyRatesCacheManagerFactory cacheManagerFactory,
                                                                           OpenExchangeRatesDateFactory openExchangeRatesDateFactory) {
        log.info("Registering bean OpenExchangeCurrencyRatesManager");
        return new OpenExchangeRatesCurrencyRatesManager(client, cacheManagerFactory, openExchangeRatesDateFactory, configuration);
    }


}
