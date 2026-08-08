package me.ezzedine.mohammed.openexchangerates4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
    OpenExchangeRatesCurrencyRatesFailureTrackerFactory openExchangeRatesCurrencyRatesFailureTrackerFactory() {
        log.info("Registering bean OpenExchangeRatesCurrencyRatesFailureTrackerFactory");
        return new OpenExchangeRatesCurrencyRatesFailureTrackerFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    OpenExchangeRatesCurrencyRatesManager openExchangeCurrencyRatesManager(OpenExchangeRatesConfiguration configuration,
                                                                           OpenExchangeRatesClient client,
                                                                           OpenExchangeRatesCurrencyRatesCacheManagerFactory cacheManagerFactory,
                                                                           OpenExchangeRatesCurrencyRatesFailureTrackerFactory failureTrackerFactory,
                                                                           OpenExchangeRatesDateFactory openExchangeRatesDateFactory) {
        log.info("Registering bean OpenExchangeCurrencyRatesManager");
        return new OpenExchangeRatesCurrencyRatesManager(client, cacheManagerFactory, failureTrackerFactory, openExchangeRatesDateFactory, configuration);
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    @ConditionalOnExpression("${openexchangerates.cache.enabled:true} and ${openexchangerates.cache.refresh.enabled:false}")
    OpenExchangeRatesCurrencyRatesRefreshScheduler openExchangeRatesCurrencyRatesRefreshScheduler(OpenExchangeRatesCurrencyRatesManager ratesManager,
                                                                                                    OpenExchangeRatesConfiguration configuration) {
        log.info("Registering bean OpenExchangeRatesCurrencyRatesRefreshScheduler");
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "openexchangerates4j-refresh");
            thread.setDaemon(true);
            return thread;
        });
        return new OpenExchangeRatesCurrencyRatesRefreshScheduler(ratesManager, configuration, executor);
    }

}
