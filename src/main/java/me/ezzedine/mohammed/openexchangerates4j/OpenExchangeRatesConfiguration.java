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

        /**
         * The proactive background refresh configuration.
         * */
        private RefreshConfiguration refresh = new RefreshConfiguration();

        /**
         * The failed fetch attempt retry configuration.
         * */
        private RetryConfiguration retry = new RetryConfiguration();

        @Data
        static class RefreshConfiguration {

            /**
             * Whether to proactively refresh the rates in the background before the cache expires,
             * using the cache lifespan as the refresh interval.
             * */
            private boolean enabled = false;
        }

        @Data
        static class RetryConfiguration {

            /**
             * The number of minutes to wait after a failed fetch attempt before retrying the server again.
             * */
            private int backoff = 5;
        }
    }
}
