package me.ezzedine.mohammed.openexchangerates4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

class OpenExchangeRatesConfigurationTest {

    @Test
    @DisplayName("it should read the properties from the application configuration correctly")
    void it_should_read_the_properties_from_the_application_configuration_correctly() {
        new ApplicationContextRunner()
                .withUserConfiguration(Configuration.class)
                .withPropertyValues(
                        "openexchangerates.appId=some-app-id",
                        "openexchangerates.cache.enabled=false",
                        "openexchangerates.cache.lifespan=300"
                )
                .run(context -> {
                    OpenExchangeRatesConfiguration configuration = context.getBean(OpenExchangeRatesConfiguration.class);
                    assertEquals("some-app-id", configuration.getAppId());
                    assertFalse(configuration.getCache().isEnabled());
                    assertEquals(300, configuration.getCache().getLifespan());
                });
    }

    @Test
    @DisplayName("it should default the cache lifespan to 120 minutes when it is not specified in the configuration")
    void it_should_default_the_cache_lifespan_to_120_minutes_when_it_is_not_specified_in_the_configuration() {
        new ApplicationContextRunner()
                .withUserConfiguration(Configuration.class)
                .withPropertyValues(
                        "openexchangerates.appId=some-app-id",
                        "openexchangerates.cache.enabled=false"
                )
                .run(context -> {
                    OpenExchangeRatesConfiguration configuration = context.getBean(OpenExchangeRatesConfiguration.class);
                    assertEquals(120, configuration.getCache().getLifespan());
                });
    }

    @Test
    @DisplayName("it should default the cache enabled to true when it is not specified in the configuration")
    void it_should_default_the_cache_enabled_to_true_when_it_is_not_specified_in_the_configuration() {
        new ApplicationContextRunner()
                .withUserConfiguration(Configuration.class)
                .withPropertyValues(
                        "openexchangerates.appId=some-app-id",
                        "openexchangerates.cache.lifespan=300"
                )
                .run(context -> {
                    OpenExchangeRatesConfiguration configuration = context.getBean(OpenExchangeRatesConfiguration.class);
                    assertTrue(configuration.getCache().isEnabled());
                });
    }

    @Test
    @DisplayName("it should return the default cache configuration when none is present in the application configuration")
    void it_should_return_the_default_cache_configuration_when_none_is_present_in_the_application_configuration() {
        new ApplicationContextRunner()
                .withUserConfiguration(Configuration.class)
                .withPropertyValues(
                        "openexchangerates.appId=some-app-id"
                )
                .run(context -> {
                    OpenExchangeRatesConfiguration configuration = context.getBean(OpenExchangeRatesConfiguration.class);
                    assertTrue(configuration.getCache().isEnabled());
                    assertEquals(120, configuration.getCache().getLifespan());
                });
    }

    @Import(OpenExchangeRatesConfiguration.class)
    @EnableConfigurationProperties
    static class Configuration {

    }
}