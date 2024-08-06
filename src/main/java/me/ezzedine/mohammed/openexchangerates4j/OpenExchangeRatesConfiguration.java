package me.ezzedine.mohammed.openexchangerates4j;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "openexchangerates")
class OpenExchangeRatesConfiguration {

    /**
     * The application ID associated with the openexchangerates account.
     * */
    private String appId;

    /**
     * The rates caching configuration.
     * */
    private CacheConfiguration cache = new CacheConfiguration();


    @Data
    static class CacheConfiguration {

        /**
         * Whether to enable or disable caching and reusing the fetched rates
         * */
        private boolean enabled = true;

        /**
         * The cache lifespan in minutes.
         * */
        private int lifespan = 120;
    }
}
